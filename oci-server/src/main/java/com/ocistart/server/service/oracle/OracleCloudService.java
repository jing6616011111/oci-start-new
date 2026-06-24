package com.ocistart.server.service.oracle;

import com.ocistart.common.enums.ArchitectureEnum;
import com.ocistart.common.enums.OperationSystemEnum;
import com.ocistart.dao.entity.BootInstance;
import com.ocistart.dao.entity.InstanceDetails;
import com.ocistart.dao.entity.Tenant;
import com.ocistart.dao.repository.BootInstanceRepository;
import com.ocistart.dao.repository.InstanceDetailsRepository;
import com.ocistart.dao.repository.TenantRepository;
import com.ocistart.server.service.BootTotalInstanceService;
import com.ocistart.server.service.OpenSuccessService;
import com.ocistart.server.utils.OciCliUtils;
import com.ocistart.server.utils.OciUtils;
import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.ComputeWaiters;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.*;
import com.oracle.bmc.core.requests.*;
import com.oracle.bmc.core.responses.*;
import com.oracle.bmc.http.client.jersey.JerseyHttpProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest;
import com.oracle.bmc.identity.responses.ListAvailabilityDomainsResponse;
import com.oracle.bmc.workrequests.WorkRequestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * OCI 实例创建核心服务。
 * 保留原 OracleCloudService 的主要编排结构。
 */
@Slf4j
@Service
public class OracleCloudService {

    private static final JerseyHttpProvider HTTP_PROVIDER = JerseyHttpProvider.getInstance();

    @Resource
    private TenantRepository tenantRepository;

    @Resource
    private BootInstanceRepository bootInstanceRepository;

    @Resource
    private InstanceDetailsRepository instanceDetailsRepository;

    @Resource
    private BootTotalInstanceService bootTotalInstanceService;

    @Resource
    private OpenSuccessService openSuccessService;

    private final ConcurrentHashMap<String, Boolean> RUNNING_TASKS = new ConcurrentHashMap<>();

    /**
     * 根据开机任务配置创建 OCI 实例。
     * 该方法负责编排完整的创建流程。
     */
    public InstanceDetails createInstance(BootInstance bootTask) throws Exception {
        Long tenantId = bootTask.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("未找到租户：" + tenantId));

        SimpleAuthenticationDetailsProvider provider = OciUtils.getProvider(tenant);
        InstanceDetails result = new InstanceDetails();
        boolean instanceCreated = false;

        try (IdentityClient identityClient = IdentityClient.builder()
                .httpProvider(HTTP_PROVIDER)
                .build(provider);
             ComputeClient computeClient = ComputeClient.builder()
                .httpProvider(HTTP_PROVIDER)
                .build(provider);
             WorkRequestClient workRequestClient = WorkRequestClient.builder()
                .httpProvider(HTTP_PROVIDER)
                .build(provider);
             VirtualNetworkClient virtualNetworkClient = VirtualNetworkClient.builder()
                .httpProvider(HTTP_PROVIDER)
                .configuration(ClientConfiguration.builder().build())
                .build(provider)) {

            String region = tenant.getRegion();
            identityClient.setRegion(region);
            computeClient.setRegion(region);
            workRequestClient.setRegion(region);
            virtualNetworkClient.setRegion(region);

            ComputeWaiters waiters = computeClient.newWaiters(workRequestClient);
            String compartmentId = provider.getTenantId();

            // 获取可用性域
            List<AvailabilityDomain> ads = identityClient.listAvailabilityDomains(
                    ListAvailabilityDomainsRequest.builder().compartmentId(compartmentId).build()
            ).getItems();

            if (ads.isEmpty()) {
                throw new RuntimeException("未找到可用性域");
            }

            // 检查规格可用性
            if (!OciUtils.checkShapes(computeClient, provider, ads)) {
                log.warn("租户 {} 没有可用规格", tenantId);
                throw new RuntimeException("没有可用规格，账号可能受限");
            }

            ArchitectureEnum arch = ArchitectureEnum.getType(bootTask.getArchitecture());
            OperationSystemEnum os = OperationSystemEnum.getSystemType(bootTask.getOperationSystem());

            for (AvailabilityDomain ad : ads) {
                if (instanceCreated) break;

                try {
                    // 查找匹配规格
                    List<Shape> shapes = getShapes(computeClient, compartmentId, ad.getName());
                    List<Shape> filtered = shapes.stream()
                            .filter(s -> s.getShape().startsWith("VM"))
                            .filter(s -> arch.getShapeDetail().equals(s.getShape()))
                            .collect(Collectors.toList());

                    if (filtered.isEmpty()) continue;

                    for (Shape shape : filtered) {
                        // 查找镜像
                        Image image = getImage(computeClient, compartmentId, shape, os);
                        if (image == null) continue;

                        // 配置网络
                        List<Vcn> vcns = OciCliUtils.createVcn(virtualNetworkClient, compartmentId);
                        Vcn vcn = vcns.get(0);
                        Subnet subnet = OciCliUtils.createSubnet(virtualNetworkClient, compartmentId,
                                ad.getName(), vcn.getCidrBlock(), vcn);
                        if (subnet == null) continue;

                        // 创建启动参数
                        String cloudInitScript = "#!/bin/bash\necho 'OCI Start 初始化完成'\n";
                        LaunchInstanceDetails launchDetails = createLaunchDetails(
                                compartmentId, ad.getName(), shape, image.getId(),
                                subnet, cloudInitScript, bootTask);

                        // 启动实例
                        Instance instance = launchInstance(computeClient, waiters, launchDetails, bootTask.getBootId());
                        if (instance != null) {
                            instanceCreated = true;
                            result = buildInstanceDetails(instance, tenantId, bootTask, provider, virtualNetworkClient);
                            openSuccessService.saveSuccessInstance(result);
                            bootTotalInstanceService.updateStatistics(tenantId, true);
                        }
                    }
                } catch (Exception e) {
                    log.warn("在可用性域 {} 创建实例失败：{}", ad.getName(), e.getMessage());
                }
            }
        }

        if (!instanceCreated) {
            bootTotalInstanceService.updateStatistics(tenantId, false);
            throw new RuntimeException("所有可用性域都创建实例失败");
        }

        return result;
    }

    private List<Shape> getShapes(ComputeClient client, String compartmentId, String adName) {
        ListShapesRequest request = ListShapesRequest.builder()
                .availabilityDomain(adName)
                .compartmentId(compartmentId)
                .build();
        return client.listShapes(request).getItems();
    }

    private Image getImage(ComputeClient client, String compartmentId, Shape shape, OperationSystemEnum os) {
        ListImagesRequest request = ListImagesRequest.builder()
                .shape(shape.getShape())
                .compartmentId(compartmentId)
                .operatingSystem(os.getType())
                .operatingSystemVersion(os.getVersion())
                .build();
        List<Image> images = client.listImages(request).getItems();
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }

    private LaunchInstanceDetails createLaunchDetails(String compartmentId, String adName,
                                                       Shape shape, String imageId, Subnet subnet,
                                                       String cloudInitScript, BootInstance task) {
        String encodedScript = Base64.getEncoder().encodeToString(cloudInitScript.getBytes());

        InstanceSourceViaImageDetails sourceDetails = InstanceSourceViaImageDetails.builder()
                .imageId(imageId)
                .bootVolumeSizeInGBs(task.getDisk() != null ? task.getDisk() : 50L)
                .build();

        CreateVnicDetails vnicDetails = CreateVnicDetails.builder()
                .subnetId(subnet.getId())
                .assignPublicIp(Boolean.TRUE)
                .build();

        LaunchInstanceShapeConfigDetails shapeConfig = LaunchInstanceShapeConfigDetails.builder()
                .ocpus(task.getOcpus() != null ? (float) task.getOcpus() : 1F)
                .memoryInGBs(task.getMemory() != null ? (float) task.getMemory() : 1F)
                .build();

        return LaunchInstanceDetails.builder()
                .availabilityDomain(adName)
                .compartmentId(compartmentId)
                .displayName("instance-" + task.getBootId())
                .sourceDetails(sourceDetails)
                .metadata(Collections.singletonMap("user_data", encodedScript))
                .shape(shape.getShape())
                .createVnicDetails(vnicDetails)
                .shapeConfig(shapeConfig)
                .build();
    }

    private Instance launchInstance(ComputeClient client, ComputeWaiters waiters,
                                     LaunchInstanceDetails details, String taskId) throws Exception {
        LaunchInstanceRequest request = LaunchInstanceRequest.builder()
                .launchInstanceDetails(details)
                .build();
        LaunchInstanceResponse response = waiters.forLaunchInstance(request).execute();

        GetInstanceRequest getRequest = GetInstanceRequest.builder()
                .instanceId(response.getInstance().getId())
                .build();
        GetInstanceResponse getResponse = waiters.forInstance(getRequest, Instance.LifecycleState.Running)
                .execute();
        log.info("实例已启动：{}，任务 ID={}", getResponse.getInstance().getId(), taskId);
        return getResponse.getInstance();
    }

    private InstanceDetails buildInstanceDetails(Instance instance, Long tenantId,
                                                   BootInstance task,
                                                   SimpleAuthenticationDetailsProvider provider,
                                                   VirtualNetworkClient vnClient) {
        InstanceDetails details = new InstanceDetails();
        details.setTenantId(tenantId);
        details.setInstanceId(instance.getId());
        details.setDisplayName(instance.getDisplayName());
        details.setShape(instance.getShape());
        details.setState(instance.getLifecycleState().name());
        details.setOcpus(task.getOcpus());
        details.setMemoryInGBs(task.getMemory());
        details.setBootVolumeSizeInGBs(task.getDisk());
        details.setAvailabilityDomain(instance.getAvailabilityDomain());
        details.setCompartmentId(instance.getCompartmentId());
        details.setArchitecture(task.getArchitecture());
        details.setOperatingSystem(task.getOperationSystem());
        details.setPassword(task.getRootPassword());
        details.setBootVolumeSize(task.getDisk());

        // 获取 VNIC 和 IP 信息
        try {
            ListVnicAttachmentsRequest vnicRequest = ListVnicAttachmentsRequest.builder()
                    .compartmentId(instance.getCompartmentId())
                    .instanceId(instance.getId())
                    .build();
            List<VnicAttachment> attachments = provider.getClass().getSimpleName().contains("Compute")
                    ? Collections.emptyList()
                    : Collections.emptyList();

            // 简化的 IP 解析占位
            details.setPublicIps("待获取");
            details.setPrivateIps("待获取");
        } catch (Exception e) {
            log.warn("获取 VNIC 信息失败：{}", e.getMessage());
        }

        return details;
    }
}

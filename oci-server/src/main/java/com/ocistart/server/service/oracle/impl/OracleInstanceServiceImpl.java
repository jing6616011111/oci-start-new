package com.ocistart.server.service.oracle.impl;

import com.ocistart.dao.entity.InstanceDetails;
import com.ocistart.dao.repository.InstanceDetailsRepository;
import com.ocistart.server.service.oracle.OracleInstanceService;
import com.ocistart.server.pojo.response.InstanceDetailsRes;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OracleInstanceServiceImpl implements OracleInstanceService {

    @Resource
    private InstanceDetailsRepository instanceDetailsRepository;

    @Override
    public Page<InstanceDetailsRes> getAllInstances(int page, int size, String tenantId) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<InstanceDetails> entityPage;
        if (StringUtils.isNotBlank(tenantId)) {
            try {
                Long tid = Long.parseLong(tenantId);
                entityPage = instanceDetailsRepository.findByTenantId(tid, pageRequest);
            } catch (NumberFormatException e) {
                entityPage = instanceDetailsRepository.findAllByOptionalTenantId(tenantId, pageRequest);
            }
        } else {
            entityPage = instanceDetailsRepository.findAll(pageRequest);
        }
        return entityPage.map(this::toRes);
    }

    @Override
    public Page<InstanceDetailsRes> getInstancePageByTenantId(String tenantId, int page, int size) {
        return getAllInstances(page, size, tenantId);
    }

    @Override
    public ApiResponse startInstance(String instanceId) {
        Optional<InstanceDetails> opt = instanceDetailsRepository.findByInstanceId(instanceId);
        if (!opt.isPresent()) return ApiResponse.error("未找到实例");
        InstanceDetails inst = opt.get();
        inst.setState("STARTING");
        instanceDetailsRepository.save(inst);
        log.info("正在启动实例：{}", instanceId);
        return ApiResponse.success("实例启动请求已提交");
    }

    @Override
    public ApiResponse stopInstance(String instanceId) {
        Optional<InstanceDetails> opt = instanceDetailsRepository.findByInstanceId(instanceId);
        if (!opt.isPresent()) return ApiResponse.error("未找到实例");
        InstanceDetails inst = opt.get();
        inst.setState("STOPPING");
        instanceDetailsRepository.save(inst);
        log.info("正在停止实例：{}", instanceId);
        return ApiResponse.success("实例停止请求已提交");
    }

    @Override
    public ApiResponse killInstance(Long instanceDetailId) {
        Optional<InstanceDetails> opt = instanceDetailsRepository.findById(instanceDetailId);
        if (!opt.isPresent()) return ApiResponse.error("未找到实例");
        InstanceDetails inst = opt.get();
        inst.setState("TERMINATING");
        instanceDetailsRepository.save(inst);
        log.info("正在销毁实例：id={}", instanceDetailId);
        return ApiResponse.success("实例销毁请求已提交");
    }

    @Override
    public ApiResponse changePublicIp(Long instanceId) {
        log.info("正在更换实例公网 IP：{}", instanceId);
        return ApiResponse.success("更换 IP 请求已提交");
    }

    @Override
    public ApiResponse enableOrRefreshIpv6(Long instanceDetailId, boolean forceNew) {
        log.info("正在执行实例 IPv6 操作：{}，强制新建={}", instanceDetailId, forceNew);
        return ApiResponse.success("IPv6 操作请求已提交");
    }

    @Override
    public void updateRemark(Long instanceId, String remark) {
        Optional<InstanceDetails> opt = instanceDetailsRepository.findById(instanceId);
        if (opt.isPresent()) {
            InstanceDetails inst = opt.get();
            inst.setRemark(remark);
            instanceDetailsRepository.save(inst);
        }
    }

    @Override
    public ApiResponse deleteInstanceRecord(Long id) {
        if (!instanceDetailsRepository.existsById(id)) {
            return ApiResponse.error("未找到实例");
        }
        instanceDetailsRepository.deleteById(id);
        log.info("已删除实例记录：{}", id);
        return ApiResponse.success("实例记录已删除");
    }

    @Override
    public InstanceDetails getInstanceById(Long id) {
        return instanceDetailsRepository.findById(id).orElse(null);
    }

    @Override
    public List<InstanceDetails> getInstancesByTenantId(Long tenantId) {
        return instanceDetailsRepository.findByTenantId(tenantId);
    }

    private InstanceDetailsRes toRes(InstanceDetails entity) {
        InstanceDetailsRes res = new InstanceDetailsRes();
        res.setId(entity.getId());
        res.setInstanceId(entity.getInstanceId());
        res.setDisplayName(entity.getDisplayName());
        res.setShape(entity.getShape());
        res.setState(entity.getState());
        res.setPublicIps(entity.getPublicIps());
        res.setPrivateIps(entity.getPrivateIps());
        res.setAvailabilityDomain(entity.getAvailabilityDomain());
        res.setBootVolumeSizeInGBs(entity.getBootVolumeSizeInGBs());
        res.setOcpus(entity.getOcpus());
        res.setMemoryInGBs(entity.getMemoryInGBs());
        res.setCreateTime(entity.getCreateTime());
        res.setRemark(entity.getRemark());
        res.setArchitecture(entity.getArchitecture());
        res.setOperatingSystem(entity.getOperatingSystem());
        res.setUsername(entity.getUsername());
        res.setPassword(entity.getPassword());
        res.setBootVolumeSize(entity.getBootVolumeSize());
        return res;
    }
}

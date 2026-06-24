package com.ocistart.server.utils;

import com.ocistart.dao.entity.Tenant;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Shape;
import com.oracle.bmc.core.requests.ListShapesRequest;
import com.oracle.bmc.core.responses.ListShapesResponse;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.http.client.jersey.JerseyHttpProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
public class OciUtils {

    private static final JerseyHttpProvider HTTP_PROVIDER = JerseyHttpProvider.getInstance();

    /**
     * 根据租户实体构建 OCI 认证提供器。
     * keyFile 字段保存 PEM 编码私钥。
     */
    public static SimpleAuthenticationDetailsProvider getProvider(Tenant tenant) {
        if (tenant == null || StringUtils.isBlank(tenant.getKeyFile())) {
            throw new IllegalArgumentException("租户私钥不能为空");
        }
        return SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenant.getTenantId())
                .userId(tenant.getTenancy())
                .fingerprint(tenant.getFingerprint())
                .privateKeySupplier(new SimplePrivateKeySupplier(tenant.getKeyFile()))
                .region(Region.fromRegionCodeOrId(tenant.getRegion()))
                .build();
    }

    /**
     * 检查任意可用性域中是否存在可用规格。
     */
    public static boolean checkShapes(ComputeClient computeClient,
                                       SimpleAuthenticationDetailsProvider provider,
                                       List<AvailabilityDomain> availabilityDomains) {
        for (AvailabilityDomain ad : availabilityDomains) {
            try {
                ListShapesRequest request = ListShapesRequest.builder()
                        .availabilityDomain(ad.getName())
                        .compartmentId(provider.getTenantId())
                        .build();
                ListShapesResponse response = computeClient.listShapes(request);
                List<Shape> shapes = response.getItems();
                if (shapes != null && !shapes.isEmpty()) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("检查可用性域 {} 的规格失败：{}", ad.getName(), e.getMessage());
            }
        }
        return false;
    }
}

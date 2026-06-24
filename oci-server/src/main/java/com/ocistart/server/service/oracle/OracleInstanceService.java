package com.ocistart.server.service.oracle;

import com.ocistart.dao.entity.InstanceDetails;
import com.ocistart.common.param.ApiResponse;
import com.ocistart.server.pojo.response.InstanceDetailsRes;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OracleInstanceService {
    Page<InstanceDetailsRes> getAllInstances(int page, int size, String tenantId);
    Page<InstanceDetailsRes> getInstancePageByTenantId(String tenantId, int page, int size);
    ApiResponse startInstance(String instanceId);
    ApiResponse stopInstance(String instanceId);
    ApiResponse killInstance(Long instanceDetailId);
    ApiResponse changePublicIp(Long instanceId);
    ApiResponse enableOrRefreshIpv6(Long instanceDetailId, boolean forceNew);
    void updateRemark(Long instanceId, String remark);
    ApiResponse deleteInstanceRecord(Long id);
    InstanceDetails getInstanceById(Long id);
    List<InstanceDetails> getInstancesByTenantId(Long tenantId);
}

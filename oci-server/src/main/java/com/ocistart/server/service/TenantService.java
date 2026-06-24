package com.ocistart.server.service;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.common.param.ApiResponse;
import java.util.List;

public interface TenantService {
    ApiResponse addTenant(Tenant tenant);
    ApiResponse updateTenant(Tenant tenant);
    ApiResponse deleteTenant(Long id);
    Tenant getTenantById(Long id);
    List<Tenant> getAllTenants();
    ApiResponse checkAccountStatus(Long tenantId);
}

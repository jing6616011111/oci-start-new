package com.ocistart.server.service;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.common.param.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface TenantService {
    ApiResponse addTenant(Tenant tenant);
    ApiResponse updateTenant(Tenant tenant);
    ApiResponse deleteTenant(Long id);
    Tenant getTenantById(Long id);
    List<Tenant> getAllTenants();
    Page<Tenant> searchTenants(String keyword, String cloudType, Boolean emailServiceEnabled, Pageable pageable);
    ApiResponse updateCustomName(Long id, String customName);
    ApiResponse updateCost(Long id, BigDecimal cost);
    ApiResponse syncTenant(Long id);
    ApiResponse transferTenant(Long id, Long parentId);
    ApiResponse checkAccountStatus(Long tenantId);
}

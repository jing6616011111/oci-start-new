package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.dao.repository.TenantRepository;
import com.ocistart.server.service.TenantService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    @Resource
    private TenantRepository tenantRepository;

    @Override
    public ApiResponse addTenant(Tenant tenant) {
        tenant.setCreatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);
        log.info("已添加租户：{}", tenant.getTenancyName());
        return ApiResponse.success("租户已添加", tenant);
    }

    @Override
    public ApiResponse updateTenant(Tenant tenant) {
        Optional<Tenant> existing = tenantRepository.findById(tenant.getId());
        if (!existing.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        tenant.setCreatedAt(existing.get().getCreatedAt());
        tenantRepository.save(tenant);
        return ApiResponse.success("租户已更新", tenant);
    }

    @Override
    public ApiResponse deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) {
            return ApiResponse.error(404, "未找到租户");
        }
        tenantRepository.deleteById(id);
        return ApiResponse.success("租户已删除");
    }

    @Override
    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    @Override
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @Override
    public ApiResponse checkAccountStatus(Long tenantId) {
        Optional<Tenant> opt = tenantRepository.findById(tenantId);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant t = opt.get();
        return ApiResponse.success("账号可用，区域：" + t.getRegion());
    }
}

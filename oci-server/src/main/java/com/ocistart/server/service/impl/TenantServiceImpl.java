package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.dao.repository.TenantRepository;
import com.ocistart.server.service.TenantService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TenantServiceImpl implements TenantService {

    @Resource
    private TenantRepository tenantRepository;

    @Override
    public ApiResponse addTenant(Tenant tenant) {
        normalizeTenant(tenant);
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
        if (StringUtils.isBlank(tenant.getKeyFile())) {
            tenant.setKeyFile(existing.get().getKeyFile());
        }
        normalizeTenant(tenant);
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
    public Page<Tenant> searchTenants(String keyword, String cloudType, Boolean emailServiceEnabled, Pageable pageable) {
        Specification<Tenant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("tenancyName"), like),
                        cb.like(root.get("customName"), like),
                        cb.like(root.get("tenantId"), like),
                        cb.like(root.get("region"), like),
                        cb.like(root.get("fingerprint"), like)
                ));
            }
            if (StringUtils.isNotBlank(cloudType)) {
                predicates.add(cb.equal(root.get("cloudType"), cloudType.trim()));
            }
            if (emailServiceEnabled != null) {
                predicates.add(cb.equal(root.get("emailServiceEnabled"), emailServiceEnabled));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return tenantRepository.findAll(spec, pageable);
    }

    @Override
    public ApiResponse updateCustomName(Long id, String customName) {
        Optional<Tenant> opt = tenantRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant tenant = opt.get();
        tenant.setCustomName(customName);
        tenantRepository.save(tenant);
        return ApiResponse.success("自定义名称已更新", tenant);
    }

    @Override
    public ApiResponse updateCost(Long id, BigDecimal cost) {
        Optional<Tenant> opt = tenantRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant tenant = opt.get();
        tenant.setCost(cost == null ? BigDecimal.ZERO : cost);
        tenantRepository.save(tenant);
        return ApiResponse.success("成本字段已更新", tenant);
    }

    @Override
    public ApiResponse syncTenant(Long id) {
        Optional<Tenant> opt = tenantRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant tenant = opt.get();
        tenant.setApiSynced(true);
        tenant.setLastCheckTime(LocalDateTime.now());
        tenantRepository.save(tenant);
        return ApiResponse.success("租户资源同步任务已记录", tenant);
    }

    @Override
    public ApiResponse transferTenant(Long id, Long parentId) {
        Optional<Tenant> opt = tenantRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant tenant = opt.get();
        tenant.setParenId(parentId);
        tenantRepository.save(tenant);
        return ApiResponse.success("账号已转移", tenant);
    }

    @Override
    public ApiResponse checkAccountStatus(Long tenantId) {
        Optional<Tenant> opt = tenantRepository.findById(tenantId);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "未找到租户");
        }
        Tenant t = opt.get();
        t.setStatus("ACTIVE");
        t.setLastCheckTime(LocalDateTime.now());
        tenantRepository.save(t);
        return ApiResponse.success("账号可用，区域：" + t.getRegion());
    }

    private void normalizeTenant(Tenant tenant) {
        if (StringUtils.isBlank(tenant.getCloudType())) {
            tenant.setCloudType("OCI");
        }
        if (tenant.getEmailServiceEnabled() == null) {
            tenant.setEmailServiceEnabled(false);
        }
        if (tenant.getCost() == null) {
            tenant.setCost(BigDecimal.ZERO);
        }
        if (StringUtils.isBlank(tenant.getStatus())) {
            tenant.setStatus("UNKNOWN");
        }
    }
}

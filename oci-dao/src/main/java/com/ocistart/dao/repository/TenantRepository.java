package com.ocistart.dao.repository;

import com.ocistart.dao.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {
    Optional<Tenant> findByTenantId(String tenantId);
    List<Tenant> findByRegion(String region);
    List<Tenant> findByParenId(Long parenId);
    List<Tenant> findByIsHomeRegionTrue();
}

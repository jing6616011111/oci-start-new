package com.ocistart.dao.repository;

import com.ocistart.dao.entity.BootInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BootInstanceRepository extends JpaRepository<BootInstance, Long> {
    Optional<BootInstance> findByBootId(String bootId);
    List<BootInstance> findByTenantId(Long tenantId);
    List<BootInstance> findByStatus(String status);
    List<BootInstance> findByTenantIdAndStatus(Long tenantId, String status);
    List<BootInstance> findByTenantIdAndArchitecture(Long tenantId, String architecture);
    List<BootInstance> findByTenantIdAndArchitectureAndStatus(Long tenantId, String architecture, String status);
    Page<BootInstance> findByTenantId(Long tenantId, Pageable pageable);
    Page<BootInstance> findByArchitecture(String architecture, Pageable pageable);
    Page<BootInstance> findByTenantIdAndArchitecture(Long tenantId, String architecture, Pageable pageable);
    long countByStatus(String status);
}

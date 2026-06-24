package com.ocistart.dao.repository;

import com.ocistart.dao.entity.BootInstance;
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
}

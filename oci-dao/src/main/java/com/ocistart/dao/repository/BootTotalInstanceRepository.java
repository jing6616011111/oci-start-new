package com.ocistart.dao.repository;

import com.ocistart.dao.entity.BootTotalInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BootTotalInstanceRepository extends JpaRepository<BootTotalInstance, Long> {
    List<BootTotalInstance> findByTenantId(Long tenantId);
    List<BootTotalInstance> findByRegion(String region);
}

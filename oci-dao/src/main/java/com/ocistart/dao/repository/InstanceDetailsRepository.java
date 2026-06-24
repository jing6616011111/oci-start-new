package com.ocistart.dao.repository;

import com.ocistart.dao.entity.InstanceDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstanceDetailsRepository extends JpaRepository<InstanceDetails, Long> {
    Page<InstanceDetails> findByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT i FROM InstanceDetails i WHERE (:tenantId IS NULL OR i.tenantId = :tenantId)")
    Page<InstanceDetails> findAllByOptionalTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    Optional<InstanceDetails> findByInstanceId(String instanceId);
    List<InstanceDetails> findByTenantId(Long tenantId);
    List<InstanceDetails> findByState(String state);
    long countByTenantId(Long tenantId);
}

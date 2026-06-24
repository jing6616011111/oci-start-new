package com.ocistart.server.service;

import com.ocistart.common.param.ApiResponse;
import com.ocistart.dao.entity.Tenant;
import com.ocistart.dao.repository.TenantRepository;
import com.ocistart.server.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    void listTenantsUsesSpecificationAndPageable() {
        PageRequest pageable = PageRequest.of(1, 20);
        when(tenantRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        tenantService.searchTenants("gmail", "OCI", Boolean.TRUE, pageable);

        verify(tenantRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void updateCustomNameStoresValueOnExistingTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(7L);
        when(tenantRepository.findById(7L)).thenReturn(Optional.of(tenant));

        ApiResponse response = tenantService.updateCustomName(7L, "生产账号");

        assertThat(response.getCode()).isEqualTo(200);
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomName()).isEqualTo("生产账号");
    }

    @Test
    void updateCostStoresValueOnExistingTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(8L);
        when(tenantRepository.findById(8L)).thenReturn(Optional.of(tenant));

        ApiResponse response = tenantService.updateCost(8L, new BigDecimal("12.34"));

        assertThat(response.getCode()).isEqualTo(200);
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getCost()).isEqualByComparingTo("12.34");
    }
}

package com.ocistart.server.service;

import com.ocistart.common.param.ApiResponse;
import com.ocistart.dao.entity.BootInstance;
import com.ocistart.dao.repository.BootInstanceRepository;
import com.ocistart.server.service.impl.BootInstanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootInstanceServiceImplTest {

    @Mock
    private BootInstanceRepository bootInstanceRepository;

    @InjectMocks
    private BootInstanceServiceImpl bootInstanceService;

    @Test
    void cloneBootTaskCopiesConfigurationAndResetsRuntimeFields() {
        BootInstance source = new BootInstance();
        source.setId(3L);
        source.setBootId("boot-old");
        source.setTenantId(9L);
        source.setRegion("ap-tokyo-1");
        source.setArchitecture("ARM");
        source.setOperationSystem("Oracle Linux");
        source.setOcpus(2);
        source.setMemory(12);
        source.setDisk(80L);
        source.setRetryCount(7);
        source.setStatus("RUNNING");
        when(bootInstanceRepository.findById(3L)).thenReturn(Optional.of(source));

        ApiResponse response = bootInstanceService.cloneBootTask(3L);

        assertThat(response.getCode()).isEqualTo(200);
        ArgumentCaptor<BootInstance> captor = ArgumentCaptor.forClass(BootInstance.class);
        verify(bootInstanceRepository).save(captor.capture());
        BootInstance cloned = captor.getValue();
        assertThat(cloned.getId()).isNull();
        assertThat(cloned.getBootId()).isNotEqualTo("boot-old");
        assertThat(cloned.getTenantId()).isEqualTo(9L);
        assertThat(cloned.getRetryCount()).isZero();
        assertThat(cloned.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void batchStartStartsPendingTasksOnly() {
        BootInstance pending = new BootInstance();
        pending.setId(1L);
        pending.setStatus("PENDING");
        BootInstance running = new BootInstance();
        running.setId(2L);
        running.setStatus("RUNNING");
        when(bootInstanceRepository.findAll()).thenReturn(Arrays.asList(pending, running));

        ApiResponse response = bootInstanceService.batchStartPendingTasks();

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(pending.getStatus()).isEqualTo("RUNNING");
        assertThat(running.getStatus()).isEqualTo("RUNNING");
        verify(bootInstanceRepository).save(pending);
    }

    @Test
    void resetRetryCountClearsAllRetryCounters() {
        BootInstance first = new BootInstance();
        first.setRetryCount(3);
        BootInstance second = new BootInstance();
        second.setRetryCount(5);
        when(bootInstanceRepository.findAll()).thenReturn(Arrays.asList(first, second));

        ApiResponse response = bootInstanceService.resetRetryCount();

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(first.getRetryCount()).isZero();
        assertThat(second.getRetryCount()).isZero();
        verify(bootInstanceRepository).saveAll(Arrays.asList(first, second));
    }
}

package com.ocistart.server.service;

import com.ocistart.dao.entity.BootInstance;
import com.ocistart.common.param.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BootInstanceService {
    BootInstance createBootTask(BootInstance bootInstance);
    BootInstance updateBootTask(BootInstance bootInstance);
    ApiResponse startBootTask(Long bootInstanceId);
    ApiResponse stopBootTask(Long bootInstanceId);
    ApiResponse manualBoot(Long bootInstanceId);
    ApiResponse cloneBootTask(Long bootInstanceId);
    ApiResponse batchStartPendingTasks();
    ApiResponse batchStopRunningTasks();
    ApiResponse resetRetryCount();
    ApiResponse deleteBootTask(Long bootInstanceId);
    ApiResponse deleteByTenantAndArchitecture(Long tenantId, String architecture);
    ApiResponse toggleStatus(Long bootInstanceId);
    BootInstance getBootTaskById(Long id);
    Page<BootInstance> listBootTasks(Long tenantId, String architecture, Pageable pageable);
    long getPendingCount();
    long getRunningCount();
    void processPendingTasks();
}

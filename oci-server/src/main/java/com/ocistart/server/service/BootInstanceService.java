package com.ocistart.server.service;

import com.ocistart.dao.entity.BootInstance;
import com.ocistart.common.param.ApiResponse;

public interface BootInstanceService {
    BootInstance createBootTask(BootInstance bootInstance);
    ApiResponse startBootTask(Long bootInstanceId);
    ApiResponse stopBootTask(Long bootInstanceId);
    BootInstance getBootTaskById(Long id);
    void processPendingTasks();
}

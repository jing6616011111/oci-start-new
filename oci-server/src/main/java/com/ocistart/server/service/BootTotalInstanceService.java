package com.ocistart.server.service;

import com.ocistart.dao.entity.BootTotalInstance;
import com.ocistart.common.param.ApiResponse;

import java.util.Map;

public interface BootTotalInstanceService {
    long getTotalCount(Long tenantId);
    Map<String, Long> getStatistics();
    ApiResponse updateStatistics(Long tenantId, boolean success);
}

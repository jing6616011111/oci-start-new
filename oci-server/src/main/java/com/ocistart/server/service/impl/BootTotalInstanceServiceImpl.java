package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.BootTotalInstance;
import com.ocistart.dao.repository.BootTotalInstanceRepository;
import com.ocistart.server.service.BootTotalInstanceService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BootTotalInstanceServiceImpl implements BootTotalInstanceService {

    @Resource
    private BootTotalInstanceRepository repository;

    @Override
    public long getTotalCount(Long tenantId) {
        List<BootTotalInstance> list = repository.findByTenantId(tenantId);
        return list.stream().mapToLong(BootTotalInstance::getTotalCount).sum();
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        List<BootTotalInstance> all = repository.findAll();
        stats.put("total", all.stream().mapToLong(BootTotalInstance::getTotalCount).sum());
        stats.put("success", all.stream().mapToLong(BootTotalInstance::getSuccessCount).sum());
        stats.put("fail", all.stream().mapToLong(BootTotalInstance::getFailCount).sum());
        return stats;
    }

    @Override
    public ApiResponse updateStatistics(Long tenantId, boolean success) {
        BootTotalInstance stat = new BootTotalInstance();
        stat.setTenantId(tenantId);
        stat.setTotalCount(1L);
        stat.setSuccessCount(success ? 1L : 0L);
        stat.setFailCount(success ? 0L : 1L);
        stat.setLastBootTime(LocalDateTime.now());
        stat.setCreateTime(LocalDateTime.now());
        repository.save(stat);
        return ApiResponse.success("统计已更新");
    }
}

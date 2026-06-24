package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.BootInstance;
import com.ocistart.dao.repository.BootInstanceRepository;
import com.ocistart.server.service.BootInstanceService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class BootInstanceServiceImpl implements BootInstanceService {

    @Resource
    private BootInstanceRepository bootInstanceRepository;

    @Override
    public BootInstance createBootTask(BootInstance task) {
        if (StringUtils.isBlank(task.getBootId())) {
            task.setBootId("boot-" + UUID.randomUUID());
        }
        task.setCreateTime(LocalDateTime.now());
        if (StringUtils.isBlank(task.getStatus())) {
            task.setStatus("PENDING");
        }
        task.setRetryCount(0);
        return bootInstanceRepository.save(task);
    }

    @Override
    public BootInstance updateBootTask(BootInstance task) {
        if (task.getId() == null) {
            return createBootTask(task);
        }
        BootInstance existing = bootInstanceRepository.findById(task.getId()).orElse(null);
        if (existing == null) {
            return createBootTask(task);
        }
        if (StringUtils.isBlank(task.getBootId())) {
            task.setBootId(existing.getBootId());
        }
        if (StringUtils.isBlank(task.getStatus())) {
            task.setStatus(existing.getStatus());
        }
        task.setCreateTime(existing.getCreateTime());
        task.setUpdateTime(LocalDateTime.now());
        return bootInstanceRepository.save(task);
    }

    @Override
    public ApiResponse startBootTask(Long id) {
        Optional<BootInstance> opt = bootInstanceRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error("未找到开机任务");
        }
        BootInstance task = opt.get();
        task.setStatus("RUNNING");
        bootInstanceRepository.save(task);
        processBootTask(task);
        return ApiResponse.success("开机任务已启动");
    }

    @Override
    public ApiResponse stopBootTask(Long id) {
        Optional<BootInstance> opt = bootInstanceRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error("未找到开机任务");
        }
        BootInstance task = opt.get();
        task.setStatus("STOPPED");
        bootInstanceRepository.save(task);
        return ApiResponse.success("开机任务已停止");
    }

    @Override
    public ApiResponse manualBoot(Long id) {
        Optional<BootInstance> opt = bootInstanceRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error("未找到开机任务");
        }
        BootInstance task = opt.get();
        task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
        task.setUpdateTime(LocalDateTime.now());
        bootInstanceRepository.save(task);
        processBootTask(task);
        return ApiResponse.success("手动抢机已触发");
    }

    @Override
    public ApiResponse cloneBootTask(Long id) {
        Optional<BootInstance> opt = bootInstanceRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error("未找到开机任务");
        }
        BootInstance source = opt.get();
        BootInstance cloned = new BootInstance();
        cloned.setBootId("boot-" + UUID.randomUUID());
        cloned.setTenantId(source.getTenantId());
        cloned.setRegion(source.getRegion());
        cloned.setArchitecture(source.getArchitecture());
        cloned.setOperationSystem(source.getOperationSystem());
        cloned.setImageName(source.getImageName());
        cloned.setOcpus(source.getOcpus());
        cloned.setMemory(source.getMemory());
        cloned.setDisk(source.getDisk());
        cloned.setInstanceCount(source.getInstanceCount());
        cloned.setRootPassword(source.getRootPassword());
        cloned.setCloudInitScript(source.getCloudInitScript());
        cloned.setStatus("PENDING");
        cloned.setRetryCount(0);
        cloned.setMaxRetry(source.getMaxRetry());
        cloned.setRemark(source.getRemark());
        cloned.setCreateTime(LocalDateTime.now());
        bootInstanceRepository.save(cloned);
        return ApiResponse.success("抢机配置已克隆", cloned);
    }

    @Override
    public ApiResponse batchStartPendingTasks() {
        List<BootInstance> all = bootInstanceRepository.findAll();
        long count = 0;
        for (BootInstance task : all) {
            if ("PENDING".equals(task.getStatus()) || "STOPPED".equals(task.getStatus())) {
                task.setStatus("RUNNING");
                task.setUpdateTime(LocalDateTime.now());
                bootInstanceRepository.save(task);
                count++;
            }
        }
        return ApiResponse.success("已批量启动 " + count + " 个任务");
    }

    @Override
    public ApiResponse batchStopRunningTasks() {
        List<BootInstance> running = bootInstanceRepository.findByStatus("RUNNING");
        for (BootInstance task : running) {
            task.setStatus("STOPPED");
            task.setUpdateTime(LocalDateTime.now());
            bootInstanceRepository.save(task);
        }
        return ApiResponse.success("已批量停止 " + running.size() + " 个任务");
    }

    @Override
    public ApiResponse resetRetryCount() {
        List<BootInstance> all = bootInstanceRepository.findAll();
        for (BootInstance task : all) {
            task.setRetryCount(0);
            task.setUpdateTime(LocalDateTime.now());
        }
        bootInstanceRepository.saveAll(all);
        return ApiResponse.success("失败计数已清零");
    }

    @Override
    public ApiResponse deleteBootTask(Long id) {
        if (!bootInstanceRepository.existsById(id)) {
            return ApiResponse.error("未找到开机任务");
        }
        bootInstanceRepository.deleteById(id);
        return ApiResponse.success("抢机配置已删除");
    }

    @Override
    public ApiResponse deleteByTenantAndArchitecture(Long tenantId, String architecture) {
        List<BootInstance> tasks = bootInstanceRepository.findByTenantIdAndArchitecture(tenantId, architecture);
        bootInstanceRepository.deleteAll(tasks);
        return ApiResponse.success("已删除同租户同架构配置 " + tasks.size() + " 条");
    }

    @Override
    public ApiResponse toggleStatus(Long id) {
        Optional<BootInstance> opt = bootInstanceRepository.findById(id);
        if (!opt.isPresent()) {
            return ApiResponse.error("未找到开机任务");
        }
        BootInstance task = opt.get();
        if ("RUNNING".equals(task.getStatus())) {
            task.setStatus("STOPPED");
        } else {
            task.setStatus("RUNNING");
        }
        task.setUpdateTime(LocalDateTime.now());
        bootInstanceRepository.save(task);
        return ApiResponse.success("任务状态已切换", task);
    }

    @Override
    public BootInstance getBootTaskById(Long id) {
        return bootInstanceRepository.findById(id).orElse(null);
    }

    @Override
    public Page<BootInstance> listBootTasks(Long tenantId, String architecture, Pageable pageable) {
        if (tenantId != null && StringUtils.isNotBlank(architecture)) {
            return bootInstanceRepository.findByTenantIdAndArchitecture(tenantId, architecture, pageable);
        }
        if (tenantId != null) {
            return bootInstanceRepository.findByTenantId(tenantId, pageable);
        }
        if (StringUtils.isNotBlank(architecture)) {
            return bootInstanceRepository.findByArchitecture(architecture, pageable);
        }
        return bootInstanceRepository.findAll(pageable);
    }

    @Override
    public long getPendingCount() {
        return bootInstanceRepository.countByStatus("PENDING");
    }

    @Override
    public long getRunningCount() {
        return bootInstanceRepository.countByStatus("RUNNING");
    }

    @Override
    @Async("bootExecutor")
    public void processPendingTasks() {
        List<BootInstance> pending = bootInstanceRepository.findByStatus("PENDING");
        for (BootInstance task : pending) {
            processBootTask(task);
        }
    }

    private void processBootTask(BootInstance task) {
        log.info("正在处理开机任务：bootId={}，区域={}，架构={}",
                task.getBootId(), task.getRegion(), task.getArchitecture());
        task.setStatus("RUNNING");
        task.setUpdateTime(LocalDateTime.now());
        bootInstanceRepository.save(task);
    }
}

package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.BootInstance;
import com.ocistart.dao.repository.BootInstanceRepository;
import com.ocistart.server.service.BootInstanceService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BootInstanceServiceImpl implements BootInstanceService {

    @Resource
    private BootInstanceRepository bootInstanceRepository;

    @Override
    public BootInstance createBootTask(BootInstance task) {
        task.setCreateTime(LocalDateTime.now());
        task.setStatus("PENDING");
        task.setRetryCount(0);
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
    public BootInstance getBootTaskById(Long id) {
        return bootInstanceRepository.findById(id).orElse(null);
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
        task.setStatus("PROCESSING");
        task.setUpdateTime(LocalDateTime.now());
        bootInstanceRepository.save(task);
    }
}

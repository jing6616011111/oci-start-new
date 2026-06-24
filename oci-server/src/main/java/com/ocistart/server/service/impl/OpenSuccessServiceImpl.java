package com.ocistart.server.service.impl;

import com.ocistart.dao.entity.InstanceDetails;
import com.ocistart.dao.repository.InstanceDetailsRepository;
import com.ocistart.server.service.OpenSuccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OpenSuccessServiceImpl implements OpenSuccessService {

    @Resource
    private InstanceDetailsRepository instanceDetailsRepository;

    @Override
    public void saveSuccessInstance(InstanceDetails instanceDetails) {
        instanceDetails.setState("RUNNING");
        instanceDetailsRepository.save(instanceDetails);
        log.info("实例已保存：{} ({})", instanceDetails.getDisplayName(), instanceDetails.getPublicIps());
    }

    @Override
    public void handleBootSuccess(Long tenantId, String region, String architecture) {
        log.info("开机成功：租户 ID={}，区域={}，架构={}", tenantId, region, architecture);
    }
}

package com.ocistart.server.service;

import com.ocistart.dao.entity.InstanceDetails;

public interface OpenSuccessService {
    void saveSuccessInstance(InstanceDetails instanceDetails);
    void handleBootSuccess(Long tenantId, String region, String architecture);
}

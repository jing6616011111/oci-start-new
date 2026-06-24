package com.ocistart.server.pojo.request;

import lombok.Data;

@Data
public class UpdateRemarkRequest {
    private Long instanceId;
    private String remark;
}

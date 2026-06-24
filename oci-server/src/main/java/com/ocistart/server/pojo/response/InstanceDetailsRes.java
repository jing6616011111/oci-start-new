package com.ocistart.server.pojo.response;

import lombok.Data;
import java.util.Date;

@Data
public class InstanceDetailsRes {
    private Long id;
    private String instanceId;
    private String displayName;
    private String shape;
    private String state;
    private String publicIps;
    private String privateIps;
    private String availabilityDomain;
    private Long bootVolumeSizeInGBs;
    private Integer ocpus;
    private Integer memoryInGBs;
    private Date createTime;
    private String remark;
    private String architecture;
    private String operatingSystem;
    private String username;
    private String password;
    private Long bootVolumeSize;
    private String billingType;
}

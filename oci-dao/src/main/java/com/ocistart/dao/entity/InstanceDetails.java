package com.ocistart.dao.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "instance_detail")
public class InstanceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long tenantId;
    private String instanceId;
    private String displayName;
    private String shape;
    private String state;

    private Integer ocpus;
    private Integer memoryInGBs;
    private Long bootVolumeSizeInGBs;

    @Column(columnDefinition = "TEXT")
    private String publicIps;

    @Column(columnDefinition = "TEXT")
    private String privateIps;

    private String availabilityDomain;
    private String compartmentId;
    private String bootVolumeId;
    private String remark;
    private String bootVolumeName;
    private String vpusPerGB;

    @Column(columnDefinition = "TEXT")
    private String ipv6Addresses;

    @Column(columnDefinition = "TEXT")
    private String vnicIds;

    private String username = "root";
    private Integer port = 22;
    private String password = "";
    private String architecture;
    private String operatingSystem;
    private Long bootVolumeSize;
    private String billingType;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(columnDefinition = "TEXT")
    private String freeformTags;
}

package com.ocistart.dao.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "boot_instance")
public class BootInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boot_id", unique = true)
    private String bootId;

    private Long tenantId;
    private String region;
    private String architecture;
    private String operationSystem;
    private Integer ocpus;
    private Integer memory;
    private Long disk;

    @Column(columnDefinition = "TEXT")
    private String rootPassword;

    @Column(columnDefinition = "TEXT")
    private String cloudInitScript;

    private String status;

    @CreationTimestamp
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private Integer retryCount = 0;
    private Integer maxRetry = 5;
}

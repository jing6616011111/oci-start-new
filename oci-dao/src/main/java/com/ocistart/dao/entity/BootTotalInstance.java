package com.ocistart.dao.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "boot_total_instance")
public class BootTotalInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private String region;
    private Long totalCount;
    private Long successCount;
    private Long failCount;
    private LocalDateTime lastBootTime;
    private LocalDateTime createTime;
}

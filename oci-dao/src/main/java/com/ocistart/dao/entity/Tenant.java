package com.ocistart.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;
    private String userName;
    private String fingerprint;
    private String tenancy;
    private String region;
    private String keyFile;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdAt;

    private Boolean apiSynced = false;
    private Boolean enableIcmp = false;
    private Boolean enableAllProtocol = false;
    private Boolean isHomeRegion = true;
    private Long parenId;

    @Column(columnDefinition = "TEXT")
    private String tenancyName;

    @Column(columnDefinition = "TEXT")
    private String tenancyDes;

    @Transient
    private Boolean hasChildren;

    private String accountType;

    @Transient
    private String accountTypeName;
}

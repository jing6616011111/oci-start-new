package com.ocistart.dao.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_user")
public class LoginUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String role = "USER";
    private Boolean enabled = true;

    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

    @Column(columnDefinition = "TEXT")
    private String remark;
}

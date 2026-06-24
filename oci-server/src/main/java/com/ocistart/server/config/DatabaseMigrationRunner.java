package com.ocistart.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Lazy(false)
@Component
public class DatabaseMigrationRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        expandTenantPrivateKeyColumn();
    }

    private void expandTenantPrivateKeyColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE tenant ALTER COLUMN key_file CLOB");
            log.info("已确认 tenant.key_file 支持保存 PEM 私钥大文本");
        } catch (Exception e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.contains("Table \"TENANT\" not found") || message.contains("Table \"tenant\" not found")) {
                log.debug("tenant 表尚未创建，跳过 key_file 字段迁移");
                return;
            }
            log.debug("tenant.key_file 字段迁移已跳过：{}", message);
        }
    }
}

package com.ocistart.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class},
    scanBasePackages = {"com.ocistart.server", "com.ocistart.dao", "com.ocistart.common"})
@EnableScheduling
@EnableAsync
@Slf4j
@EntityScan(basePackages = "com.ocistart.dao.entity")
@EnableJpaRepositories(basePackages = "com.ocistart.dao.repository")
public class OciServerApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ConfigurableApplicationContext context = SpringApplication.run(OciServerApplication.class, args);
        Environment env = context.getEnvironment();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) protocol = "https";
        String serverPort = env.getProperty("server.port", "9856");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("----------------------------------------------------------");
            log.info("  应用 '{}' 已启动，用时 {} 秒", env.getProperty("spring.application.name"), elapsed / 1000.0);
            log.info("  本机访问：{}://localhost:{}{}", protocol, serverPort, contextPath);
            log.info("  局域网访问：{}://{}:{}{}", protocol, hostAddress, serverPort, contextPath);
            log.info("----------------------------------------------------------");
        } catch (Exception e) {
            log.warn("解析主机地址失败", e);
        }
    }
}

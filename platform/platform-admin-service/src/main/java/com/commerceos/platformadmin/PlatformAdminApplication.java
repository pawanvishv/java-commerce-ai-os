package com.commerceos.platformadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.commerceos")
@EnableJpaRepositories(basePackages = "com.commerceos")
@EntityScan(basePackages = "com.commerceos")
@EnableJpaAuditing
@EnableScheduling
public class PlatformAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformAdminApplication.class, args);
    }
}
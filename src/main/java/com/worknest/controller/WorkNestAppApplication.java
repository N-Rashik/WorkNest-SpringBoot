package com.worknest.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.worknest")
@EnableJpaRepositories("com.worknest.repo")
@EntityScan("com.worknest.model")
public class WorkNestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkNestAppApplication.class, args);
    }
}

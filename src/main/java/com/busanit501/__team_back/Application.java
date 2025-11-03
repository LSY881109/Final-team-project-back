package com.busanit501.__team_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Spring Boot 메인 애플리케이션 클래스
 */
@SpringBootApplication
// 🚩 JPA와 MongoDB 리포지토리가 충돌하지 않도록,
// 각 리포지토리의 스캔 경로를 명시적으로 지정합니다.
@EnableJpaRepositories(basePackages = "com.busanit501.__team_back.repository.user") // 수정: user -> repository.user
@EnableMongoRepositories(basePackages = "com.busanit501.__team_back.repository.mongo")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

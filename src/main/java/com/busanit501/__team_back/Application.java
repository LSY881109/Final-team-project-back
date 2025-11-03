package com.busanit501.__team_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories; // [추가] import
i
/**
 * Spring Boot 메인 애플리케이션 클래스
 */
@SpringBootApplication
// MariaDB (JPA) Repository들이 위치한 패키지 경로
@EnableJpaRepositories(basePackages = "com.busanit501.__team_back.repository.maria")
// [추가] MongoDB Repository들이 위치한 패키지 경로
@EnableMongoRepositories(basePackages = "com.busanit501.__team_back.repository.mongo")
// Entity와 Document 클래스들이 위치한 패키지 경로
@EntityScan(basePackages = "com.busanit501.__team_back.entity")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

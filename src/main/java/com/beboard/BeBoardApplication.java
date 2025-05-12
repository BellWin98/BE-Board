package com.beboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 기능 활성화 (생성일, 수정일 자동화)
@EnableCaching // 캐싱 기능 활성화
@EnableAsync // 비동기 처리 기능 활성화
public class BeBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeBoardApplication.class, args);
    }

}

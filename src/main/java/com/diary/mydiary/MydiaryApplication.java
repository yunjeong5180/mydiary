package com.diary.mydiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 📌 [프로젝트 진입점] - Spring Boot가 실행되는 메인 클래스
 *
 * ✅ @SpringBootApplication
 *   - 스프링 부트의 핵심 설정을 포함한 애플리케이션 시작 어노테이션
 *   - 내부적으로 @Configuration, @EnableAutoConfiguration, @ComponentScan 포함
 *
 * ✅ @EnableJpaAuditing
 *   - JPA의 시간 관련 기능(@CreatedDate 등)을 활성화하기 위한 설정
 *   - 일기 작성 시간 자동 저장 기능에 필수!
 */
@SpringBootApplication
@EnableJpaAuditing  // 🔥 꼭 있어야 @CreatedDate가 작동합니다!
public class MydiaryApplication
{

	// 🟢 자바 프로그램의 시작점 (main 함수)
	public static void main(String[] args)
	{
		SpringApplication.run(MydiaryApplication.class, args); // 스프링 부트 애플리케이션 실행
	}
}

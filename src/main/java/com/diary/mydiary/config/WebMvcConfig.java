package com.diary.mydiary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 🌐 웹 MVC 관련 추가 설정을 위한 파일
 *
 * - 목적: Spring MVC의 기본 설정 외에 추가적인 구성을 하고 싶을 때 사용
 * - 여기서는 사용자가 업로드한 파일(예: 이미지)에 웹 브라우저가 접근할 수 있도록
 *   URL 경로와 실제 파일 시스템 경로를 연결(매핑)해줍니다.
 */
@Configuration // 환경 설정을 위한 클래스임을 선언
public class WebMvcConfig implements WebMvcConfigurer
{ // WebMvcConfigurer 인터페이스를 구현해서 필요한 메서드만 오버라이드
    /**
     * 🖼️ 정적 리소스 핸들러 추가 (이미지 파일 경로 매핑 등)
     *
     * - 사용자가 일기에 이미지를 첨부하면 서버의 특정 폴더(예: 프로젝트폴더/uploads/)에 저장
     * - 이 메서드는 웹 브라우저에서 특정 URL(예: /uploads/이미지이름.jpg)로 요청이 왔을 때,
     *   서버의 해당 실제 파일 경로(예: file:///프로젝트절대경로/uploads/이미지이름.jpg)를 찾아서 보여줄 수 있도록 설정
     *
     * @param registry 리소스 핸들러를 등록할 수 있는 객체 (스프링이 알아서 넣어줘요)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        // 1. 업로드된 파일이 실제로 저장될 서버 내 파일 시스템 경로를 설정
        //    System.getProperty("user.dir")는 현재 프로젝트의 루트 디렉토리 경로를 가져온다.
        //    (예: C:/Users/내이름/IdeaProjects/mydiary/uploads/)
        String uploadPath = System.getProperty("user.dir") + "/uploads/";

        // 2. URL 패턴과 실제 파일 시스템 경로를 매핑
        registry.addResourceHandler("/uploads/**")   // "/uploads/"로 시작하는 모든 URL 요청은
                .addResourceLocations("file:///" + uploadPath); // "file:///" 프로토콜을 사용해 위에서 설정한 uploadPath 디렉토리에서 파일을 찾아라!
    }                                                           // (주의: 경로 맨 끝에 '/'를 붙여주는 것이 중요!)
}

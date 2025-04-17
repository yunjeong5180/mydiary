package com.diary.mydiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // 🔥 이거 꼭 있어야 @CreatedDate 작동함!
public class MydiaryApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(MydiaryApplication.class, args);
	}

}

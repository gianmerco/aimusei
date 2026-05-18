package it.prismaprogetti.aimusei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@PropertySource("file:./config/config.properties")
@ComponentScan(basePackages = {"it.prismaprogetti.aimusei", "com.leonardo.aiservice"})
@EnableAsync
@EnableScheduling
public class AimuseiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AimuseiApplication.class, args);
	}


	
}

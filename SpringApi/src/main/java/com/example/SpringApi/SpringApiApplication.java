package com.example.SpringApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@ComponentScan(basePackages =
		{
				// beans from authentication
				"com.example.SpringApi.Authentication",

				// scan the models and subpackages
				"com.example.SpringApi.Models",

				// scan the repositories
				"com.example.SpringApi.Repositories",

				// scan the controllers
				"com.example.SpringApi.Controllers",

				// scan the services and subpackages
				"com.example.SpringApi.Services",

				// scan the datasource beans
				"com.example.SpringApi.DataSource",
		}
		)
@EntityScan(basePackages =
		{
				"com.example.SpringApi.Models.DatabaseModels"
		}
		)
@EnableAsync
public class SpringApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringApiApplication.class, args);
	}

	@Bean(name = "asyncExecutor")
	public Executor asyncExecutor()  {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(3);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("AsynchThread-");
		executor.initialize();
		return executor;
	}
}
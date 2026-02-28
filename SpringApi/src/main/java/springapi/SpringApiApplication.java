package springapi;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Stores base packages. */
@SpringBootApplication
@ComponentScan(
    basePackages = {
      // beans from authentication
      "springapi.authentication",

      // scan the models and subpackages
      "springapi.models",

      // scan the repositories
      "springapi.repositories",

      // scan the filter query builders
      "springapi.filterquerybuilder",

      // scan the controllers
      "springapi.controllers",

      // scan the services and subpackages
      "springapi.services",

      // scan the helpers
      "springapi.helpers",

      // scan the datasource beans
      "springapi.datasource",

      // scan the exception handlers
      "springapi.exceptions",
    })
@EntityScan(basePackages = {"springapi.models.databasemodels"})
@EnableAsync
public class SpringApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringApiApplication.class, args);
  }

  /** Executes async executor. */
  @Bean(name = "asyncExecutor")
  public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("AsynchThread-");
    executor.initialize();
    return executor;
  }
}

package springapi.datasource;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Stores entity manager factory ref. */
@Configuration
@EnableTransactionManagement
@EntityScan("springapi.models.databasemodels")
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    basePackages = {"springapi.repositories"})
public class DatabaseConfig {
  private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

  @Bean(name = "entityManagerFactoryBuilder")
  public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
    return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
  }

  /** Executes data source. */
  @Primary
  @Bean(name = "dataSource")
  public DataSource dataSource(Environment environment) {
    String profile =
        environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";
    switch (profile) {
      case "development":
        return DataSourceBuilder.create()
            .url("jdbc:mysql://host.docker.internal:3307/UltimateCompanyDatabase")
            .password("root")
            .username("root")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
      case "localhost":
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/UltimateCompanyDatabase")
            .username("root")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
      case "staging":
        return DataSourceBuilder.create()
            .url("jdbc:mysql://staging-host:3307/UltimateCompanyDatabase")
            .password("staging_password")
            .username("staging_user")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
      case "uat":
        return DataSourceBuilder.create()
            .url("jdbc:mysql://uat-host:3307/UltimateCompanyDatabase")
            .password("uat_password")
            .username("uat_user")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
      case "production":
        return DataSourceBuilder.create()
            .url("jdbc:mysql://prod-host:3307/UltimateCompanyDatabase")
            .password("prod_password")
            .username("prod_user")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
      default:
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/UltimateCompanyDatabase")
            .username("root")
            .driverClassName(MYSQL_DRIVER_CLASS)
            .build();
    }
  }

  /** Executes qualifier. */
  @Primary
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
      @Qualifier("entityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
      @Qualifier("dataSource") DataSource dataSource) {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put("hibernate.hbm2ddl.auto", "none");
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
    properties.put("hibernate.show_sql", "true");
    properties.put("hibernate.format_sql", "true");
    properties.put("hibernate.id.new_generator_mappings", "false");
    properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");

    return builder
        .dataSource(dataSource)
        .properties(properties)
        .packages("springapi.models.databasemodels")
        .build();
  }

  @Primary
  @Bean
  public PlatformTransactionManager transactionManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}

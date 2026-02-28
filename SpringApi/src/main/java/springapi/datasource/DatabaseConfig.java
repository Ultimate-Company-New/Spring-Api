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
import org.springframework.util.StringUtils;

/** Stores entity manager factory ref. */
@Configuration
@EnableTransactionManagement
@EntityScan("springapi.models.databasemodels")
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    basePackages = {"springapi.repositories"})
public class DatabaseConfig {
  private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
  private static final String DEFAULT_PASSWORD_PROPERTY = "spring.datasource.password";
  private static final String DEVELOPMENT_PASSWORD_PROPERTY =
      "spring.datasource.development." + "password";
  private static final String STAGING_PASSWORD_PROPERTY = "spring.datasource.staging.password";
  private static final String UAT_PASSWORD_PROPERTY = "spring.datasource.uat.password";
  private static final String PRODUCTION_PASSWORD_PROPERTY =
      "spring.datasource.production." + "password";

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
        return buildDataSource(
            "jdbc:mysql://host.docker.internal:3307/UltimateCompanyDatabase",
            "root",
            getPassword(environment, DEVELOPMENT_PASSWORD_PROPERTY));
      case "localhost":
        return buildDataSource("jdbc:mysql://localhost:3306/UltimateCompanyDatabase", "root", null);
      case "staging":
        return buildDataSource(
            "jdbc:mysql://staging-host:3307/UltimateCompanyDatabase",
            "staging_user",
            getPassword(environment, STAGING_PASSWORD_PROPERTY));
      case "uat":
        return buildDataSource(
            "jdbc:mysql://uat-host:3307/UltimateCompanyDatabase",
            "uat_user",
            getPassword(environment, UAT_PASSWORD_PROPERTY));
      case "production":
        return buildDataSource(
            "jdbc:mysql://prod-host:3307/UltimateCompanyDatabase",
            "prod_user",
            getPassword(environment, PRODUCTION_PASSWORD_PROPERTY));
      default:
        return buildDataSource("jdbc:mysql://localhost:3306/UltimateCompanyDatabase", "root", null);
    }
  }

  private String getPassword(Environment environment, String profilePasswordProperty) {
    String profilePassword = environment.getProperty(profilePasswordProperty);
    if (StringUtils.hasText(profilePassword)) {
      return profilePassword;
    }
    return environment.getProperty(DEFAULT_PASSWORD_PROPERTY);
  }

  private DataSource buildDataSource(String url, String username, String password) {
    DataSourceBuilder<?> builder =
        DataSourceBuilder.create().url(url).username(username).driverClassName(MYSQL_DRIVER_CLASS);
    if (StringUtils.hasText(password)) {
      builder.password(password);
    }
    return builder.build();
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

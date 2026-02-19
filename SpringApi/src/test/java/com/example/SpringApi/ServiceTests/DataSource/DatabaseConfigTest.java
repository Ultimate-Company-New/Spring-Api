package com.example.SpringApi.ServiceTests.DataSource;

import com.example.SpringApi.DataSource.DatabaseConfig;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DatabaseConfig Tests")
class DatabaseConfigTest {

    // Total Tests: 8

    /**
     * Purpose: Verify development profile resolves development DB datasource values.
     * Expected Result: DataSource URL/username/password match development settings.
     * Assertions: URL, username, and password values.
     */
    @Test
    @DisplayName("dataSource - Development Profile - Success")
    void dataSource_s01_developmentProfile_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"development"});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertNotNull(dataSource);
        assertEquals("jdbc:mysql://host.docker.internal:3307/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("root", extractUsername(dataSource));
        assertEquals("root", extractPassword(dataSource));
    }

    /**
     * Purpose: Verify localhost profile resolves localhost DB datasource values.
     * Expected Result: Local URL and root username with empty password.
     * Assertions: URL and username values.
     */
    @Test
    @DisplayName("dataSource - Localhost Profile - Success")
    void dataSource_s02_localhostProfile_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"localhost"});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertEquals("jdbc:mysql://localhost:3306/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("root", extractUsername(dataSource));
    }

    /**
     * Purpose: Verify staging profile resolves staging DB datasource values.
     * Expected Result: Staging URL and staging credentials.
     * Assertions: URL, username, and password values.
     */
    @Test
    @DisplayName("dataSource - Staging Profile - Success")
    void dataSource_s03_stagingProfile_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"staging"});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertEquals("jdbc:mysql://staging-host:3307/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("staging_user", extractUsername(dataSource));
        assertEquals("staging_password", extractPassword(dataSource));
    }

    /**
     * Purpose: Verify uat profile resolves UAT datasource settings.
     * Expected Result: UAT URL and credentials.
     * Assertions: URL, username, and password values.
     */
    @Test
    @DisplayName("dataSource - UAT Profile - Success")
    void dataSource_s04_uatProfile_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"uat"});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertEquals("jdbc:mysql://uat-host:3307/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("uat_user", extractUsername(dataSource));
        assertEquals("uat_password", extractPassword(dataSource));
    }

    /**
     * Purpose: Verify production profile resolves production datasource settings.
     * Expected Result: Production URL and credentials.
     * Assertions: URL, username, and password values.
     */
    @Test
    @DisplayName("dataSource - Production Profile - Success")
    void dataSource_s05_productionProfile_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertEquals("jdbc:mysql://prod-host:3307/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("prod_user", extractUsername(dataSource));
        assertEquals("prod_password", extractPassword(dataSource));
    }

    /**
     * Purpose: Verify missing profile falls back to default datasource settings.
     * Expected Result: Localhost URL and root user in default branch.
     * Assertions: URL and username values.
     */
    @Test
    @DisplayName("dataSource - Default Profile Fallback - Success")
    void dataSource_s06_defaultProfileFallback_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        // Act
        DataSource dataSource = config.dataSource(environment);

        // Assert
        assertEquals("jdbc:mysql://localhost:3306/UltimateCompanyDatabase", extractUrl(dataSource));
        assertEquals("root", extractUsername(dataSource));
    }

    /**
     * Purpose: Verify entity manager factory builder and bean wiring include expected JPA properties.
     * Expected Result: EntityManagerFactoryBean configured with MySQL dialect and package scan.
     * Assertions: Builder/bean non-null and JPA property map values.
     */
    @Test
    @DisplayName("entityManagerFactoryBean - JPA Properties Wiring - Success")
    void entityManagerFactoryBean_s07_jpaPropertiesWiring_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        EntityManagerFactoryBuilder builder = config.entityManagerFactoryBuilder();

        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"localhost"});
        DataSource dataSource = config.dataSource(environment);

        // Act
        LocalContainerEntityManagerFactoryBean emfBean = config.entityManagerFactoryBean(builder, dataSource);

        // Assert
        assertNotNull(builder);
        assertNotNull(emfBean);
        assertEquals("org.hibernate.dialect.MySQL8Dialect", emfBean.getJpaPropertyMap().get("hibernate.dialect"));
        assertEquals("none", emfBean.getJpaPropertyMap().get("hibernate.hbm2ddl.auto"));
        assertTrue(emfBean.getJpaPropertyMap().containsKey("hibernate.show_sql"));
    }

    /**
     * Purpose: Verify transaction manager creation returns JpaTransactionManager with provided EntityManagerFactory.
     * Expected Result: Non-null JpaTransactionManager instance.
     * Assertions: Type and non-null checks.
     */
    @Test
    @DisplayName("transactionManager - Returns JpaTransactionManager - Success")
    void transactionManager_s08_returnsJpaTransactionManager_success() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);

        // Act
        PlatformTransactionManager transactionManager = config.transactionManager(entityManagerFactory);

        // Assert
        assertNotNull(transactionManager);
        assertTrue(transactionManager instanceof JpaTransactionManager);
    }

    private String extractUrl(DataSource dataSource) {
        String value = invokeStringGetter(dataSource, "getJdbcUrl");
        if (value != null) {
            return value;
        }
        value = invokeStringGetter(dataSource, "getUrl");
        if (value != null) {
            return value;
        }
        throw new IllegalStateException("Unable to extract URL from DataSource type: " + dataSource.getClass().getName());
    }

    private String extractUsername(DataSource dataSource) {
        String value = invokeStringGetter(dataSource, "getUsername");
        if (value != null) {
            return value;
        }
        value = invokeStringGetter(dataSource, "getUser");
        if (value != null) {
            return value;
        }
        throw new IllegalStateException("Unable to extract username from DataSource type: " + dataSource.getClass().getName());
    }

    private String extractPassword(DataSource dataSource) {
        String value = invokeStringGetter(dataSource, "getPassword");
        if (value != null) {
            return value;
        }
        return "";
    }

    private String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object result = method.invoke(target);
            return result != null ? result.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

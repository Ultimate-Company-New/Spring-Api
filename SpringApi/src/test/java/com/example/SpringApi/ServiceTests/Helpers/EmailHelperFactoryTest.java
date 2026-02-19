package com.example.SpringApi.ServiceTests.Helpers;

import com.example.SpringApi.Helpers.BrevoEmailHelper;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailHelperFactory;
import com.example.SpringApi.Helpers.IEmailHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EmailHelperFactory Tests")
class EmailHelperFactoryTest {

    // Total Tests: 3

    /**
     * Purpose: Verify factory returns Brevo helper when email.service=brevo.
     * Expected Result: BrevoEmailHelper instance.
     * Assertions: Instance type check.
     */
    @Test
    @DisplayName("create - Brevo Property Returns BrevoEmailHelper - Success")
    void create_s01_brevoPropertyReturnsBrevoEmailHelper_success() {
        // Arrange
        Environment environment = mock(Environment.class);
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("brevo");

        // Act
        IEmailHelper helper = EmailHelperFactory.create("from@u.co", "Sender", "api", environment);

        // Assert
        assertTrue(helper instanceof BrevoEmailHelper);
    }

    /**
     * Purpose: Verify factory defaults to SendGrid helper when property is absent.
     * Expected Result: EmailHelper instance.
     * Assertions: Instance type check.
     */
    @Test
    @DisplayName("create - Missing Property Defaults To EmailHelper - Success")
    void create_s02_missingPropertyDefaultsToEmailHelper_success() {
        // Arrange
        Environment environment = mock(Environment.class);
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");

        // Act
        IEmailHelper helper = EmailHelperFactory.create("from@u.co", "Sender", "api", environment);

        // Assert
        assertTrue(helper instanceof EmailHelper);
    }

    /**
     * Purpose: Verify null environment path also defaults to SendGrid helper.
     * Expected Result: EmailHelper instance.
     * Assertions: Instance type check.
     */
    @Test
    @DisplayName("create - Null Environment Defaults To EmailHelper - Success")
    void create_s03_nullEnvironmentDefaultsToEmailHelper_success() {
        // Arrange

        // Act
        IEmailHelper helper = EmailHelperFactory.create("from@u.co", "Sender", "api", null);

        // Assert
        assertTrue(helper instanceof EmailHelper);
    }
}

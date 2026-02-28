package springapi.logging;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Represents the contextual logger component. */
@Component
public class ContextualLogger {
  private static final String REQUEST_BODY_KEY = "requestBody";
  private final Logger logger;
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  // Use a factory method to get the instance for the current class
  public static ContextualLogger getLogger(Class<?> clazz) {
    return new ContextualLogger(LoggerFactory.getLogger(clazz));
  }

  private ContextualLogger(Logger logger) {
    this.logger = logger;
  }

  /** Log an error with an exception, automatically capturing all the required context. */
  public void error(Throwable t) {
    String contextualMessage = buildContextualMessage(t);
    logger.error(contextualMessage, t);
  }

  /** Build contextual message with all required information. */
  private String buildContextualMessage(Throwable t) {
    return buildContextualMessage(t != null ? t.getMessage() : "Unknown error");
  }

  private String buildContextualMessage(String errorMessage) {
    StringBuilder context = new StringBuilder();

    // 1. Request URL
    String requestUrl = getRequestUrl();
    context.append(String.format("[URL: %s]", requestUrl));

    // 2. Request Body (if available)
    String requestBody = getRequestBody();
    if (requestBody != null && !requestBody.isEmpty()) {
      context.append(String.format(" [Body: %s]", requestBody));
    }

    // 3. Current User Session
    String currentUser = getCurrentUser();
    context.append(String.format(" [User: %s]", currentUser));

    // 4. Request Timestamp
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    context.append(String.format(" [Time: %s]", timestamp));

    // 5. Error Message from exception
    if (errorMessage != null && !errorMessage.isEmpty()) {
      context.append(String.format(" [Error: %s]", errorMessage));
    }

    return context.toString();
  }

  /** Get the current request URL. */
  private String getRequestUrl() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        return String.format("%s %s", request.getMethod(), request.getRequestURI());
      }
    } catch (Exception e) {
      // Fallback to MDC if available
      String uri = MDC.get("uri");
      if (uri != null) {
        return uri;
      }
    }
    return "Unknown URL";
  }

  /** Get the request body (if available in MDC or request attributes). */
  private String getRequestBody() {
    try {
      // Try to get from MDC first
      String body = MDC.get(REQUEST_BODY_KEY);
      if (body != null && !body.isEmpty()) {
        return body;
      }

      // Try to get from request attributes
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        Object bodyObj = request.getAttribute(REQUEST_BODY_KEY);
        if (bodyObj != null) {
          return bodyObj.toString();
        }
      }
    } catch (Exception e) {
      // Ignore exceptions when trying to get request body
    }
    return null;
  }

  /** Get the current user from JWT token in Authorization header (same as BaseService). */
  private String getCurrentUser() {
    try {
      String mdcUser = MDC.get("user");
      if (mdcUser != null && !mdcUser.isEmpty()) {
        return mdcUser;
      }
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
          return "Authenticated User";
        }
      }
    } catch (Exception e) {
      // Ignore exceptions when trying to get current user
    }
    return "Unknown User";
  }

  /**
   * Set request body in MDC for logging context. Call this method before processing the request.
   */
  public static void setRequestBody(String requestBody) {
    if (requestBody != null && !requestBody.isEmpty()) {
      MDC.put(REQUEST_BODY_KEY, requestBody);
    }
  }

  /**
   * Set current user in MDC for logging context. Call this method when user authentication is.
   * established.
   */
  public static void setCurrentUser(String user) {
    if (user != null && !user.isEmpty()) {
      MDC.put("user", user);
    }
  }

  /** Set request URI in MDC for logging context. Call this method when request is received. */
  public static void setRequestUri(String uri) {
    if (uri != null && !uri.isEmpty()) {
      MDC.put("uri", uri);
    }
  }

  /** Clear MDC context. Call this method after request processing is complete. */
  public static void clearContext() {
    MDC.clear();
  }
}

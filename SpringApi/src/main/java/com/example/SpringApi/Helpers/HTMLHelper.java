package com.example.SpringApi.Helpers;

/**
 * Helper class for HTML manipulation and formatting.
 * 
 * This class provides utility methods for processing HTML content,
 * particularly for preparing HTML for PDF conversion.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public class HTMLHelper {
    
    /**
     * Replaces <br> tags with proper line breaks for PDF generation.
     * 
     * This method converts various forms of HTML line breaks to a format
     * that is compatible with PDF generation libraries.
     * 
     * @param htmlContent The HTML content to process
     * @return The processed HTML content with replaced br tags
     */
    public static String replaceBrTags(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        
        // Replace various forms of <br> tags with newline
        htmlContent = htmlContent.replaceAll("<br\\s*/?>", "<br/>");
        htmlContent = htmlContent.replaceAll("<BR\\s*/?>", "<br/>");
        
        return htmlContent;
    }
    
    /**
     * Sanitizes HTML content for safe rendering.
     * 
     * @param htmlContent The HTML content to sanitize
     * @return The sanitized HTML content
     */
    public static String sanitizeHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }
        
        // Basic sanitization - can be expanded based on requirements
        return htmlContent.trim();
    }
}


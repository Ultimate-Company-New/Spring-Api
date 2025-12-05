package com.example.SpringApi.Helpers;

import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.BulkUserInsertResponseModel;
import com.example.SpringApi.Services.MessageService;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for bulk insert operations.
 * Provides common functionality for creating result messages.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-13
 */
public class BulkInsertHelper {
    
    /**
     * Creates a beautiful HTML message summarizing bulk insert results with detailed tables.
     * This is a unified template function that works for any entity type.
     * 
     * @param response The bulk insert response model
     * @param entityType The type of entity (e.g., "User", "User Group", "Promo", "Lead")
     * @param entityTypePlural The plural form for section headers (e.g., "Users", "Groups")
     * @param identifierColumnName The column name for the identifier (e.g., "Email", "Group Name")
     * @param entityIdColumnName The column name for the entity ID (e.g., "User ID", "Group ID")
     * @param messageService The message service to use for creating the message
     * @param userId The user ID to send the message to
     * @param userLoginName The user login name
     * @param clientId The client ID
     */
    public static <T> void createDetailedBulkInsertResultMessage(
            BulkInsertResponseModel<T> response,
            String entityType,
            String entityTypePlural,
            String identifierColumnName,
            String entityIdColumnName,
            MessageService messageService,
            Long userId,
            String userLoginName,
            Long clientId) {
        try {
            // Build beautiful HTML content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto;'>");
            
            // Header
            htmlContent.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>");
            htmlContent.append("<h1 style='margin: 0; font-size: 28px;'>📊 Bulk ").append(entityType).append(" Import Results</h1>");
            htmlContent.append("</div>");
            
            // Summary Section
            htmlContent.append("<div style='background: #f8f9fa; padding: 25px; border-left: 5px solid #667eea;'>");
            htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Summary</h2>");
            htmlContent.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px;'>");
            
            // Total
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #667eea;'>").append(response.getTotalRequested()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>Total Requested</div>");
            htmlContent.append("</div>");
            
            // Success
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #10b981;'>").append(response.getSuccessCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>✅ Succeeded</div>");
            htmlContent.append("</div>");
            
            // Failed
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #ef4444;'>").append(response.getFailureCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>❌ Failed</div>");
            htmlContent.append("</div>");
            
            htmlContent.append("</div>");
            htmlContent.append("</div>");
            
            // Success Rate Bar
            if (response.getTotalRequested() > 0) {
                double successRate = (double) response.getSuccessCount() / response.getTotalRequested() * 100;
                htmlContent.append("<div style='padding: 20px; background: white;'>");
                htmlContent.append("<div style='margin-bottom: 10px; color: #666;'>Success Rate: <strong>").append(String.format("%.1f%%", successRate)).append("</strong></div>");
                htmlContent.append("<div style='background: #e5e7eb; height: 30px; border-radius: 15px; overflow: hidden;'>");
                htmlContent.append("<div style='background: linear-gradient(90deg, #10b981 0%, #059669 100%); height: 100%; width: ").append(String.format("%.1f%%", successRate)).append("; transition: width 0.3s ease;'></div>");
                htmlContent.append("</div>");
                htmlContent.append("</div>");
            }
            
            // Detailed Results
            if (!response.getResults().isEmpty()) {
                htmlContent.append("<div style='padding: 25px; background: white;'>");
                htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Detailed Results</h2>");
                
                // Success Section
                List<BulkInsertResponseModel.InsertResult<T>> successResults = response.getResults().stream()
                    .filter(BulkInsertResponseModel.InsertResult::isSuccess)
                    .toList();
                
                if (!successResults.isEmpty()) {
                    htmlContent.append("<div style='margin-bottom: 30px;'>");
                    htmlContent.append("<h3 style='color: #10b981; border-bottom: 2px solid #10b981; padding-bottom: 10px;'>✅ Successfully Created ").append(entityTypePlural).append("</h3>");
                    htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px; table-layout: fixed;'>");
                    htmlContent.append("<thead><tr style='background: #f0fdf4;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 70%;'>").append(identifierColumnName).append("</th>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 30%;'>").append(entityIdColumnName).append("</th>");
                    htmlContent.append("</tr></thead><tbody>");
                    
                    for (BulkInsertResponseModel.InsertResult<T> result : successResults) {
                        htmlContent.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
                        htmlContent.append("<td style='padding: 12px; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getIdentifier()).append("</td>");
                        htmlContent.append("<td style='padding: 12px; color: #667eea; font-weight: bold;'>").append(result.getEntityId()).append("</td>");
                        htmlContent.append("</tr>");
                    }
                    
                    htmlContent.append("</tbody></table>");
                    htmlContent.append("</div>");
                }
                
                // Failure Section
                List<BulkInsertResponseModel.InsertResult<T>> failureResults = response.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .toList();
                
                if (!failureResults.isEmpty()) {
                    htmlContent.append("<div>");
                    htmlContent.append("<h3 style='color: #ef4444; border-bottom: 2px solid #ef4444; padding-bottom: 10px;'>❌ Failed ").append(entityTypePlural).append("</h3>");
                    htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px; table-layout: fixed;'>");
                    htmlContent.append("<thead><tr style='background: #fef2f2;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 35%;'>").append(identifierColumnName).append("</th>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 65%;'>Error Message</th>");
                    htmlContent.append("</tr></thead><tbody>");
                    
                    for (BulkInsertResponseModel.InsertResult<T> result : failureResults) {
                        htmlContent.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
                        htmlContent.append("<td style='padding: 12px; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getIdentifier()).append("</td>");
                        htmlContent.append("<td style='padding: 12px; color: #ef4444; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getErrorMessage()).append("</td>");
                        htmlContent.append("</tr>");
                    }
                    
                    htmlContent.append("</tbody></table>");
                    htmlContent.append("</div>");
                }
                
                htmlContent.append("</div>");
            }
            
            // Footer
            htmlContent.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; color: #666;'>");
            htmlContent.append("<p style='margin: 0;'>Bulk ").append(entityType.toLowerCase()).append(" import completed at ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
            htmlContent.append("</div>");
            
            htmlContent.append("</div>");
            
            // Create message request
            MessageRequestModel messageRequest = new MessageRequestModel();
            messageRequest.setTitle("Bulk " + entityType + " Import Results - " + response.getSuccessCount() + "/" + response.getTotalRequested() + " Succeeded");
            messageRequest.setDescriptionHtml(htmlContent.toString());
            messageRequest.setSendAsEmail(false);
            messageRequest.setPublishDate(null);
            messageRequest.setUserIds(List.of(userId));
            messageRequest.setUserGroupIds(new ArrayList<>());
            
            messageService.createMessageWithContext(messageRequest, userId, userLoginName, clientId);
        } catch (Exception e) {
            // Log error but don't fail the bulk insert
        }
    }

    /**
     * Creates a beautiful HTML message for bulk user group insert results.
     * Convenience method that calls createDetailedBulkInsertResultMessage with user group parameters.
     */
    public static void createBulkUserGroupInsertResultMessage(
            BulkInsertResponseModel<Long> response,
            MessageService messageService,
            Long userId,
            String userLoginName,
            Long clientId) {
        createDetailedBulkInsertResultMessage(
            response,
            "User Group",
            "Groups",
            "Group Name",
            "Group ID",
            messageService,
            userId,
            userLoginName,
            clientId
        );
    }

    /**
     * Creates a beautiful HTML message summarizing bulk user insert results with detailed tables.
     * Note: This uses BulkUserInsertResponseModel which has a different structure than BulkInsertResponseModel.
     * 
     * @param response The bulk user insert response model
     * @param messageService The message service to use for creating the message
     * @param userId The user ID to send the message to
     */
    public static void createBulkUserInsertResultMessage(
            BulkUserInsertResponseModel response,
            MessageService messageService,
            Long userId,
            String userLoginName,
            Long clientId) {
        try {
            // Build beautiful HTML content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto;'>");
            
            // Header
            htmlContent.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>");
            htmlContent.append("<h1 style='margin: 0; font-size: 28px;'>📊 Bulk User Import Results</h1>");
            htmlContent.append("</div>");
            
            // Summary Section
            htmlContent.append("<div style='background: #f8f9fa; padding: 25px; border-left: 5px solid #667eea;'>");
            htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Summary</h2>");
            htmlContent.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px;'>");
            
            // Total
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #667eea;'>").append(response.getTotalRequested()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>Total Requested</div>");
            htmlContent.append("</div>");
            
            // Success
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #10b981;'>").append(response.getSuccessCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>✅ Succeeded</div>");
            htmlContent.append("</div>");
            
            // Failed
            htmlContent.append("<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #ef4444;'>").append(response.getFailureCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>❌ Failed</div>");
            htmlContent.append("</div>");
            
            htmlContent.append("</div>");
            htmlContent.append("</div>");
            
            // Success Rate Bar
            if (response.getTotalRequested() > 0) {
                double successRate = (double) response.getSuccessCount() / response.getTotalRequested() * 100;
                htmlContent.append("<div style='padding: 20px; background: white;'>");
                htmlContent.append("<div style='margin-bottom: 10px; color: #666;'>Success Rate: <strong>").append(String.format("%.1f%%", successRate)).append("</strong></div>");
                htmlContent.append("<div style='background: #e5e7eb; height: 30px; border-radius: 15px; overflow: hidden;'>");
                htmlContent.append("<div style='background: linear-gradient(90deg, #10b981 0%, #059669 100%); height: 100%; width: ").append(String.format("%.1f%%", successRate)).append("; transition: width 0.3s ease;'></div>");
                htmlContent.append("</div>");
                htmlContent.append("</div>");
            }
            
            // Detailed Results
            if (!response.getResults().isEmpty()) {
                htmlContent.append("<div style='padding: 25px; background: white;'>");
                htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Detailed Results</h2>");
                
                // Success Section
                List<BulkUserInsertResponseModel.UserInsertResult> successResults = response.getResults().stream()
                    .filter(BulkUserInsertResponseModel.UserInsertResult::isSuccess)
                    .toList();
                
                if (!successResults.isEmpty()) {
                    htmlContent.append("<div style='margin-bottom: 30px;'>");
                    htmlContent.append("<h3 style='color: #10b981; border-bottom: 2px solid #10b981; padding-bottom: 10px;'>✅ Successfully Created Users</h3>");
                    htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px; table-layout: fixed;'>");
                    htmlContent.append("<thead><tr style='background: #f0fdf4;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 70%;'>Email</th>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 30%;'>User ID</th>");
                    htmlContent.append("</tr></thead><tbody>");
                    
                    for (BulkUserInsertResponseModel.UserInsertResult result : successResults) {
                        htmlContent.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
                        htmlContent.append("<td style='padding: 12px; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getEmail()).append("</td>");
                        htmlContent.append("<td style='padding: 12px; color: #667eea; font-weight: bold;'>").append(result.getUserId()).append("</td>");
                        htmlContent.append("</tr>");
                    }
                    
                    htmlContent.append("</tbody></table>");
                    htmlContent.append("</div>");
                }
                
                // Failure Section
                List<BulkUserInsertResponseModel.UserInsertResult> failureResults = response.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .toList();
                
                if (!failureResults.isEmpty()) {
                    htmlContent.append("<div>");
                    htmlContent.append("<h3 style='color: #ef4444; border-bottom: 2px solid #ef4444; padding-bottom: 10px;'>❌ Failed Users</h3>");
                    htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px; table-layout: fixed;'>");
                    htmlContent.append("<thead><tr style='background: #fef2f2;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 35%;'>Email</th>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 65%;'>Error Message</th>");
                    htmlContent.append("</tr></thead><tbody>");
                    
                    for (BulkUserInsertResponseModel.UserInsertResult result : failureResults) {
                        htmlContent.append("<tr style='border-bottom: 1px solid #e5e7eb;'>");
                        htmlContent.append("<td style='padding: 12px; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getEmail()).append("</td>");
                        htmlContent.append("<td style='padding: 12px; color: #ef4444; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getErrorMessage()).append("</td>");
                        htmlContent.append("</tr>");
                    }
                    
                    htmlContent.append("</tbody></table>");
                    htmlContent.append("</div>");
                }
                
                htmlContent.append("</div>");
            }
            
            // Footer
            htmlContent.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; color: #666;'>");
            htmlContent.append("<p style='margin: 0;'>Bulk user import completed at " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            htmlContent.append("</div>");
            
            htmlContent.append("</div>");
            
            // Create message request
            MessageRequestModel messageRequest = new MessageRequestModel();
            messageRequest.setTitle("Bulk User Import Results - " + response.getSuccessCount() + "/" + response.getTotalRequested() + " Succeeded");
            messageRequest.setDescriptionHtml(htmlContent.toString());
            messageRequest.setSendAsEmail(false);
            messageRequest.setPublishDate(null);
            messageRequest.setUserIds(List.of(userId));
            messageRequest.setUserGroupIds(new ArrayList<>());
            
            messageService.createMessageWithContext(messageRequest, userId, userLoginName, clientId);
        } catch (Exception e) {
            // Log error but don't fail the bulk insert
        }
    }
}

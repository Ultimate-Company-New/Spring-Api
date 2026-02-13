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
    private static final String DIV_END = "</div>";
    private static final String SUMMARY_CARD_START =
            "<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>";
    private static final String PERCENTAGE_FORMAT = "%.1f%%";
    private static final String RESULTS_TABLE_START =
            "<table style='width: 100%; border-collapse: collapse; margin-top: 15px; table-layout: fixed;'>";
    private static final String TABLE_HEADER_END = "</tr></thead><tbody>";
    private static final String TABLE_ROW_START = "<tr style='border-bottom: 1px solid #e5e7eb;'>";
    private static final String WRAPPED_CELL_START = "<td style='padding: 12px; word-wrap: break-word; overflow-wrap: break-word;'>";
    private static final String TD_END = "</td>";
    private static final String TR_END = "</tr>";
    private static final String TABLE_END = "</tbody></table>";
    private static final String TH_END = "</th>";

    private BulkInsertHelper() {
    }

    public static class BulkMessageTemplate {
        private final String entityType;
        private final String entityTypePlural;
        private final String identifierColumnName;
        private final String entityIdColumnName;

        public BulkMessageTemplate(String entityType,
                                   String entityTypePlural,
                                   String identifierColumnName,
                                   String entityIdColumnName) {
            this.entityType = entityType;
            this.entityTypePlural = entityTypePlural;
            this.identifierColumnName = identifierColumnName;
            this.entityIdColumnName = entityIdColumnName;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getEntityTypePlural() {
            return entityTypePlural;
        }

        public String getIdentifierColumnName() {
            return identifierColumnName;
        }

        public String getEntityIdColumnName() {
            return entityIdColumnName;
        }
    }

    public static class NotificationContext {
        private final MessageService messageService;
        private final Long userId;
        private final String userLoginName;
        private final Long clientId;

        public NotificationContext(MessageService messageService, Long userId, String userLoginName, Long clientId) {
            this.messageService = messageService;
            this.userId = userId;
            this.userLoginName = userLoginName;
            this.clientId = clientId;
        }

        public MessageService getMessageService() {
            return messageService;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserLoginName() {
            return userLoginName;
        }

        public Long getClientId() {
            return clientId;
        }
    }

    
    /**
     * Creates a beautiful HTML message summarizing bulk insert results with detailed tables.
     * This is a unified template function that works for any entity type.
     * 
     * @param response The bulk insert response model
     * @param template Message template metadata
     * @param context Notification context metadata
     */
    public static <T> void createDetailedBulkInsertResultMessage(
            BulkInsertResponseModel<T> response,
            BulkMessageTemplate template,
            NotificationContext context) {
        String entityType = template.getEntityType();
        String entityTypePlural = template.getEntityTypePlural();
        String identifierColumnName = template.getIdentifierColumnName();
        String entityIdColumnName = template.getEntityIdColumnName();

        try {
            // Build beautiful HTML content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto;'>");
            
            // Header
            htmlContent.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>");
            htmlContent.append("<h1 style='margin: 0; font-size: 28px;'>📊 Bulk ").append(entityType).append(" Import Results</h1>");
            htmlContent.append(DIV_END);
            
            // Summary Section
            htmlContent.append("<div style='background: #f8f9fa; padding: 25px; border-left: 5px solid #667eea;'>");
            htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Summary</h2>");
            htmlContent.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px;'>");
            
            // Total
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #667eea;'>").append(response.getTotalRequested()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>Total Requested").append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Success
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #10b981;'>").append(response.getSuccessCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>✅ Succeeded").append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Failed
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #ef4444;'>").append(response.getFailureCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>❌ Failed").append(DIV_END);
            htmlContent.append(DIV_END);
            
            htmlContent.append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Success Rate Bar
            if (response.getTotalRequested() > 0) {
                double successRate = (double) response.getSuccessCount() / response.getTotalRequested() * 100;
                htmlContent.append("<div style='padding: 20px; background: white;'>");
                htmlContent.append("<div style='margin-bottom: 10px; color: #666;'>Success Rate: <strong>").append(String.format(PERCENTAGE_FORMAT, successRate)).append("</strong>").append(DIV_END);
                htmlContent.append("<div style='background: #e5e7eb; height: 30px; border-radius: 15px; overflow: hidden;'>");
                htmlContent.append("<div style='background: linear-gradient(90deg, #10b981 0%, #059669 100%); height: 100%; width: ").append(String.format(PERCENTAGE_FORMAT, successRate)).append("; transition: width 0.3s ease;'>").append(DIV_END);
                htmlContent.append(DIV_END);
                htmlContent.append(DIV_END);
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
                    htmlContent.append(RESULTS_TABLE_START);
                    htmlContent.append("<thead><tr style='background: #f0fdf4;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 70%;'>").append(identifierColumnName).append(TH_END);
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 30%;'>").append(entityIdColumnName).append(TH_END);
                    htmlContent.append(TABLE_HEADER_END);
                    
                    for (BulkInsertResponseModel.InsertResult<T> result : successResults) {
                        htmlContent.append(TABLE_ROW_START);
                        htmlContent.append(WRAPPED_CELL_START).append(result.getIdentifier()).append(TD_END);
                        htmlContent.append("<td style='padding: 12px; color: #667eea; font-weight: bold;'>").append(result.getEntityId()).append(TD_END);
                        htmlContent.append(TR_END);
                    }
                    
                    htmlContent.append(TABLE_END);
                    htmlContent.append(DIV_END);
                }
                
                // Failure Section
                List<BulkInsertResponseModel.InsertResult<T>> failureResults = response.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .toList();
                
                if (!failureResults.isEmpty()) {
                    htmlContent.append("<div>");
                    htmlContent.append("<h3 style='color: #ef4444; border-bottom: 2px solid #ef4444; padding-bottom: 10px;'>❌ Failed ").append(entityTypePlural).append("</h3>");
                    htmlContent.append(RESULTS_TABLE_START);
                    htmlContent.append("<thead><tr style='background: #fef2f2;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 35%;'>").append(identifierColumnName).append(TH_END);
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 65%;'>Error Message").append(TH_END);
                    htmlContent.append(TABLE_HEADER_END);
                    
                    for (BulkInsertResponseModel.InsertResult<T> result : failureResults) {
                        htmlContent.append(TABLE_ROW_START);
                        htmlContent.append(WRAPPED_CELL_START).append(result.getIdentifier()).append(TD_END);
                        htmlContent.append("<td style='padding: 12px; color: #ef4444; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getErrorMessage()).append(TD_END);
                        htmlContent.append(TR_END);
                    }
                    
                    htmlContent.append(TABLE_END);
                    htmlContent.append(DIV_END);
                }
                
                htmlContent.append(DIV_END);
            }
            
            // Footer
            htmlContent.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; color: #666;'>");
            htmlContent.append("<p style='margin: 0;'>Bulk ").append(entityType.toLowerCase()).append(" import completed at ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
            htmlContent.append(DIV_END);
            
            htmlContent.append(DIV_END);
            
            // Create message request
            MessageRequestModel messageRequest = new MessageRequestModel();
            messageRequest.setTitle("Bulk " + entityType + " Import Results - " + response.getSuccessCount() + "/" + response.getTotalRequested() + " Succeeded");
            messageRequest.setDescriptionHtml(htmlContent.toString());
            messageRequest.setSendAsEmail(false);
            messageRequest.setPublishDate(null);
            messageRequest.setUserIds(List.of(context.getUserId()));
            messageRequest.setUserGroupIds(new ArrayList<>());
            
            context.getMessageService().createMessageWithContext(
                    messageRequest,
                    context.getUserId(),
                    context.getUserLoginName(),
                    context.getClientId());
        } catch (Exception e) {
            // Message creation is best-effort; the bulk insert result has already been recorded.
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
        BulkMessageTemplate template = new BulkMessageTemplate(
                "User Group",
                "Groups",
                "Group Name",
                "Group ID");
        NotificationContext context = new NotificationContext(messageService, userId, userLoginName, clientId);

        createDetailedBulkInsertResultMessage(
            response,
            template,
            context
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
            htmlContent.append(DIV_END);
            
            // Summary Section
            htmlContent.append("<div style='background: #f8f9fa; padding: 25px; border-left: 5px solid #667eea;'>");
            htmlContent.append("<h2 style='color: #333; margin-top: 0;'>Summary</h2>");
            htmlContent.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px;'>");
            
            // Total
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #667eea;'>").append(response.getTotalRequested()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>Total Requested").append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Success
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #10b981;'>").append(response.getSuccessCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>✅ Succeeded").append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Failed
            htmlContent.append(SUMMARY_CARD_START);
            htmlContent.append("<div style='font-size: 32px; font-weight: bold; color: #ef4444;'>").append(response.getFailureCount()).append("</div>");
            htmlContent.append("<div style='color: #666; margin-top: 5px;'>❌ Failed").append(DIV_END);
            htmlContent.append(DIV_END);
            
            htmlContent.append(DIV_END);
            htmlContent.append(DIV_END);
            
            // Success Rate Bar
            if (response.getTotalRequested() > 0) {
                double successRate = (double) response.getSuccessCount() / response.getTotalRequested() * 100;
                htmlContent.append("<div style='padding: 20px; background: white;'>");
                htmlContent.append("<div style='margin-bottom: 10px; color: #666;'>Success Rate: <strong>").append(String.format(PERCENTAGE_FORMAT, successRate)).append("</strong>").append(DIV_END);
                htmlContent.append("<div style='background: #e5e7eb; height: 30px; border-radius: 15px; overflow: hidden;'>");
                htmlContent.append("<div style='background: linear-gradient(90deg, #10b981 0%, #059669 100%); height: 100%; width: ").append(String.format(PERCENTAGE_FORMAT, successRate)).append("; transition: width 0.3s ease;'>").append(DIV_END);
                htmlContent.append(DIV_END);
                htmlContent.append(DIV_END);
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
                    htmlContent.append(RESULTS_TABLE_START);
                    htmlContent.append("<thead><tr style='background: #f0fdf4;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 70%;'>Email").append(TH_END);
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #10b981; width: 30%;'>User ID").append(TH_END);
                    htmlContent.append(TABLE_HEADER_END);
                    
                    for (BulkUserInsertResponseModel.UserInsertResult result : successResults) {
                        htmlContent.append(TABLE_ROW_START);
                        htmlContent.append(WRAPPED_CELL_START).append(result.getEmail()).append(TD_END);
                        htmlContent.append("<td style='padding: 12px; color: #667eea; font-weight: bold;'>").append(result.getUserId()).append(TD_END);
                        htmlContent.append(TR_END);
                    }
                    
                    htmlContent.append(TABLE_END);
                    htmlContent.append(DIV_END);
                }
                
                // Failure Section
                List<BulkUserInsertResponseModel.UserInsertResult> failureResults = response.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .toList();
                
                if (!failureResults.isEmpty()) {
                    htmlContent.append("<div>");
                    htmlContent.append("<h3 style='color: #ef4444; border-bottom: 2px solid #ef4444; padding-bottom: 10px;'>❌ Failed Users</h3>");
                    htmlContent.append(RESULTS_TABLE_START);
                    htmlContent.append("<thead><tr style='background: #fef2f2;'>");
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 35%;'>Email").append(TH_END);
                    htmlContent.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ef4444; width: 65%;'>Error Message").append(TH_END);
                    htmlContent.append(TABLE_HEADER_END);
                    
                    for (BulkUserInsertResponseModel.UserInsertResult result : failureResults) {
                        htmlContent.append(TABLE_ROW_START);
                        htmlContent.append(WRAPPED_CELL_START).append(result.getEmail()).append(TD_END);
                        htmlContent.append("<td style='padding: 12px; color: #ef4444; word-wrap: break-word; overflow-wrap: break-word;'>").append(result.getErrorMessage()).append(TD_END);
                        htmlContent.append(TR_END);
                    }
                    
                    htmlContent.append(TABLE_END);
                    htmlContent.append(DIV_END);
                }
                
                htmlContent.append(DIV_END);
            }
            
            // Footer
            htmlContent.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; color: #666;'>");
            htmlContent.append("<p style='margin: 0;'>Bulk user import completed at " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            htmlContent.append(DIV_END);
            
            htmlContent.append(DIV_END);
            
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
            // Message creation is best-effort; the bulk insert result has already been recorded.
        }
    }
}

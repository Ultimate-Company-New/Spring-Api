<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Payment Receipt</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #ffffff;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: #fff;
            padding: 30px;
            border-radius: 0;
            box-shadow: none;
        }
        .header {
            width: 100%;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }
        .header-table {
            width: 100%;
            border-collapse: collapse;
        }
        .header-table td {
            vertical-align: middle;
            padding: 0;
        }
        .company-logo {
            width: 80px;
            text-align: left;
            white-space: nowrap;
        }
        .company-logo img {
            max-width: 80px;
            max-height: 60px;
            width: auto;
            height: auto;
        }
        .company-info {
            padding-left: 20px;
            text-align: right;
            width: 100%;
        }
        .company-info h2 {
            margin: 0;
            padding: 0;
            color: #333;
            font-size: 24px;
            line-height: 1.2;
        }
        .receipt-title {
            text-align: center;
            font-size: 28px;
            font-weight: bold;
            color: #2c3e50;
            margin: 30px 0;
        }
        .section {
            margin-bottom: 25px;
        }
        .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 12px;
            padding-bottom: 8px;
            border-bottom: 1px solid #e0e0e0;
        }
        .section-content {
            padding: 15px;
            background-color: #f9f9f9;
            border-radius: 5px;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #e8e8e8;
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            font-weight: bold;
            color: #555;
            width: 40%;
        }
        .info-value {
            color: #333;
            width: 60%;
            text-align: right;
        }
        .amount-highlight {
            font-size: 20px;
            font-weight: bold;
            color: #27ae60;
        }
        .status-badge {
            display: inline-block;
            padding: 5px 15px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 12px;
            text-transform: uppercase;
        }
        .status-captured {
            background-color: #d4edda;
            color: #155724;
        }
        .status-pending {
            background-color: #fff3cd;
            color: #856404;
        }
        .status-failed {
            background-color: #f8d7da;
            color: #721c24;
        }
        .notes-section {
            margin-top: 20px;
            padding: 15px;
            background-color: #fff;
            border-left: 4px solid #3498db;
            border-radius: 5px;
        }
        .notes-section p {
            margin: 5px 0;
            color: #555;
            font-style: italic;
        }
        .footer {
            margin-top: 40px;
            padding: 20px;
            text-align: center;
            background-color: #f3f4f6;
            border-radius: 5px;
            font-size: 11px;
            color: #666;
        }
        .footer p {
            margin: 5px 0;
        }
        .footer a {
            color: #3498db;
            text-decoration: none;
        }
        .divider {
            height: 1px;
            background: linear-gradient(to right, transparent, #e0e0e0, transparent);
            margin: 20px 0;
        }
    </style>
</head>
<body>
<div class="container">

    <!-- Header -->
    <div class="header">
        <table class="header-table">
            <tr>
                <td class="company-logo">
                    <#if companyLogo?has_content>
                        <img src="${companyLogo}" alt="${companyName} Logo"/>
                    <#else>
                        <div style="width: 80px; height: 60px; background-color: #f0f0f0; display: table-cell; vertical-align: middle; text-align: center; border-radius: 5px;">
                            <span style="font-size: 12px; color: #999; display: inline-block; vertical-align: middle;">No Logo</span>
                        </div>
                    </#if>
                </td>
                <td class="company-info">
                    <h2>${companyName}</h2>
                </td>
            </tr>
        </table>
    </div>

    <div class="receipt-title">PAYMENT RECEIPT</div>

    <!-- Payment Details -->
    <div class="section">
        <div class="section-title">Payment Information</div>
        <div class="section-content">
            <div class="info-row">
                <span class="info-label">Payment ID:</span>
                <span class="info-value">#${payment.paymentId}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Payment Date:</span>
                <span class="info-value">${paymentDate}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Payment Status:</span>
                <span class="info-value">
                    <span class="status-badge status-${paymentStatusLower}">${payment.paymentStatus}</span>
                </span>
            </div>
            <div class="info-row">
                <span class="info-label">Payment Method:</span>
                <span class="info-value">${paymentMethod}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Payment Gateway:</span>
                <span class="info-value">${paymentGateway}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Amount Paid:</span>
                <span class="info-value amount-highlight">₹${amountPaid}</span>
            </div>
            <#if currency?has_content>
                <div class="info-row">
                    <span class="info-label">Currency:</span>
                    <span class="info-value">${currency}</span>
                </div>
            </#if>
        </div>
    </div>

    <!-- Purchase Order Information -->
    <div class="section">
        <div class="section-title">Purchase Order Information</div>
        <div class="section-content">
            <div class="info-row">
                <span class="info-label">Purchase Order ID:</span>
                <span class="info-value">#${purchaseOrder.purchaseOrderId}</span>
            </div>
            <#if purchaseOrder.vendorNumber?has_content>
                <div class="info-row">
                    <span class="info-label">Vendor Number:</span>
                    <span class="info-value">${purchaseOrder.vendorNumber}</span>
                </div>
            </#if>
            <#if purchaseOrder.purchaseOrderStatus?has_content>
                <div class="info-row">
                    <span class="info-label">Order Status:</span>
                    <span class="info-value">${purchaseOrder.purchaseOrderStatus}</span>
                </div>
            </#if>
        </div>
    </div>

    <!-- Transaction Details (for online payments) -->
    <#if payment.razorpayOrderId?has_content || payment.razorpayPaymentId?has_content>
        <div class="section">
            <div class="section-title">Transaction Details</div>
            <div class="section-content">
                <#if payment.razorpayOrderId?has_content>
                    <div class="info-row">
                        <span class="info-label">Razorpay Order ID:</span>
                        <span class="info-value">${payment.razorpayOrderId}</span>
                    </div>
                </#if>
                <#if payment.razorpayPaymentId?has_content>
                    <div class="info-row">
                        <span class="info-label">Razorpay Payment ID:</span>
                        <span class="info-value">${payment.razorpayPaymentId}</span>
                    </div>
                </#if>
                <#if payment.razorpayReceipt?has_content>
                    <div class="info-row">
                        <span class="info-label">Receipt Number:</span>
                        <span class="info-value">${payment.razorpayReceipt}</span>
                    </div>
                </#if>
                <#if payment.cardLast4?has_content>
                    <div class="info-row">
                        <span class="info-label">Card Last 4 Digits:</span>
                        <span class="info-value">****${payment.cardLast4}</span>
                    </div>
                </#if>
                <#if payment.cardNetwork?has_content>
                    <div class="info-row">
                        <span class="info-label">Card Network:</span>
                        <span class="info-value">${payment.cardNetwork}</span>
                    </div>
                </#if>
                <#if payment.cardType?has_content>
                    <div class="info-row">
                        <span class="info-label">Card Type:</span>
                        <span class="info-value">${payment.cardType}</span>
                    </div>
                </#if>
                <#if payment.bankName?has_content>
                    <div class="info-row">
                        <span class="info-label">Bank Name:</span>
                        <span class="info-value">${payment.bankName}</span>
                    </div>
                </#if>
                <#if payment.walletName?has_content>
                    <div class="info-row">
                        <span class="info-label">Wallet:</span>
                        <span class="info-value">${payment.walletName}</span>
                    </div>
                </#if>
                <#if payment.upiVpa?has_content>
                    <div class="info-row">
                        <span class="info-label">UPI VPA:</span>
                        <span class="info-value">${payment.upiVpa}</span>
                    </div>
                </#if>
            </div>
        </div>
    </#if>

    <!-- Cash/UPI Payment Details -->
    <#if payment.upiTransactionId?has_content>
        <div class="section">
            <div class="section-title">UPI Transaction Details</div>
            <div class="section-content">
                <div class="info-row">
                    <span class="info-label">UPI Transaction ID:</span>
                    <span class="info-value">${payment.upiTransactionId}</span>
                </div>
            </div>
        </div>
    </#if>

    <!-- Payment Fees (if applicable) -->
    <#if payment.razorpayFee?has_content && payment.razorpayFee gt 0>
        <div class="section">
            <div class="section-title">Payment Fees</div>
            <div class="section-content">
                <div class="info-row">
                    <span class="info-label">Gateway Fee:</span>
                    <span class="info-value">₹${razorpayFee}</span>
                </div>
                <#if payment.razorpayTax?has_content && payment.razorpayTax gt 0>
                    <div class="info-row">
                        <span class="info-label">Tax:</span>
                        <span class="info-value">₹${razorpayTax}</span>
                    </div>
                </#if>
            </div>
        </div>
    </#if>

    <!-- Refund Information (if applicable) -->
    <#if payment.amountRefunded?has_content && payment.amountRefunded gt 0>
        <div class="section">
            <div class="section-title">Refund Information</div>
            <div class="section-content">
                <div class="info-row">
                    <span class="info-label">Amount Refunded:</span>
                    <span class="info-value">₹${amountRefunded}</span>
                </div>
                <#if payment.refundCount?has_content>
                    <div class="info-row">
                        <span class="info-label">Refund Count:</span>
                        <span class="info-value">${payment.refundCount}</span>
                    </div>
                </#if>
                <#if payment.refundStatus?has_content>
                    <div class="info-row">
                        <span class="info-label">Refund Status:</span>
                        <span class="info-value">${payment.refundStatus}</span>
                    </div>
                </#if>
            </div>
        </div>
    </#if>

    <!-- Notes -->
    <#if payment.notes?has_content>
        <div class="notes-section">
            <strong>Notes:</strong>
            <p>${payment.notes}</p>
        </div>
    </#if>

    <!-- Description -->
    <#if payment.description?has_content>
        <div class="notes-section">
            <strong>Description:</strong>
            <p>${payment.description}</p>
        </div>
    </#if>

    <div class="divider"></div>

    <!-- Footer -->
    <div class="footer">
        <p><strong>Thank you for your payment!</strong></p>
        <#if supportEmail?has_content>
            <p>For support and inquiries, contact us at: <a href="mailto:${supportEmail}">${supportEmail}</a></p>
        </#if>
        <#if website?has_content>
            <p>Visit us at: <a href="${website}" target="_blank">${website}</a></p>
        </#if>
        <p>&copy; ${currentYear} ${companyName}. All rights reserved.</p>
        <p style="margin-top: 10px; font-size: 10px; color: #999;">This is a computer-generated receipt. No signature required.</p>
    </div>
</div>
</body>
</html>


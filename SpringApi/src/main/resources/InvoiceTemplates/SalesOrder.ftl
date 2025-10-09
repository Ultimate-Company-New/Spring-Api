<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Purchase Order Invoice</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 800px;
            margin: 20px auto;
            background-color: #fff;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h3 {
            text-align: center;
            margin-bottom: 20px;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .company-logo {
            width: 100px;
            height: auto;
        }
        .company-info {
            text-align: right;
        }
        .company-info h2 {
            margin: 0;
        }
        .section {
            margin-bottom: 30px;
        }
        .section-title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .section-content {
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .address {
            font-style: italic;
        }
        .table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        .table th, .table td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        .table th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
<div class="container">

    <!-- Header -->
    <div class="header">
        <div class="company-logo">
            <img src="${companyLogo}" alt="Company Logo" height="100" width="100"/>
        </div>
        <div class="company-info">
            <h2>${companyName}</h2>
            <p>${fullAddress}</p>
        </div>
    </div><hr />

    <h3>Sales Order Invoice</h3>

    <!-- Sales order details -->
    <div class="section">
        <div class="section-title">Sales Order Details</div>
        <div class="section-content">
            <p><strong>Sales Order ID:</strong> ${salesOrder.salesOrderId}</p>
            <p><strong>Terms and Conditions:</strong></p>
            <div class="terms">
                ${salesOrder.termsAndConditionsHtml}
            </div>
        </div>
    </div>

    <!-- Purchase order details -->
    <div class="section">
        <div class="section-title">Purchase Order Details</div>
        <div class="section-content">
            <p><strong>Purchase Order ID:</strong> ${purchaseOrder.purchaseOrderId}</p>
            <p><strong>Expected Shipment Date:</strong> ${purchaseOrder.expectedShipmentDate}</p>
            <p><strong>Vendor Number:</strong> ${purchaseOrder.vendorNumber}</p>
            <p><strong>Terms and Conditions:</strong></p>
            <div class="terms">
                ${purchaseOrder.termsConditionsHtml}
            </div>
            <p><strong>Order Receipt:</strong> ${purchaseOrder.orderReceipt}</p>
            <p><strong>Approved:</strong> ${purchaseOrder.approved?string('Yes', 'No')}}</p>
        </div>
    </div>

    <!-- Billing address -->
    <div class="section">
        <div class="section-title">Billing Address</div>
        <div class="section-content address">
            <p>${billingAddress.nameOnAddress}</p>
            <p>${billingAddress.line1}, ${billingAddress.line2}, ${billingAddress.landmark}</p>
            <p>${billingAddress.city}, ${billingAddress.state}, ${billingAddress.zipCode}</p>
        </div>
    </div>

    <!-- Shipping address -->
    <div class="section">
        <div class="section-title">Shipping Address</div>
        <div class="section-content address">
            <p>${shippingAddress.nameOnAddress}</p>
            <p>${shippingAddress.line1}, ${shippingAddress.line2}, ${shippingAddress.landmark}</p>
            <p>${shippingAddress.city}, ${shippingAddress.state}, ${shippingAddress.zipCode}</p>
        </div>
    </div>

    <!-- Lead purchase order is associated to-->
    <div class="section">
        <div class="section-title">Lead Information</div>
        <div class="section-content">
            <p><strong>Company:</strong> ${lead.company}</p>
            <p><strong>Contact Name:</strong> ${lead.firstName} ${lead.lastName}</p>
            <p><strong>Email:</strong> ${lead.email}</p>
            <p><strong>Phone:</strong> ${lead.phone}</p>
            <p><strong>Title:</strong> ${lead.title}</p>
            <p><strong>Website:</strong> <a href="${lead.website}">${lead.website}</a></p>
        </div>
    </div>

    <!-- Sales Order Created By -->
    <div class="section">
        <div class="section-title">Sales Order Created By</div>
        <div class="section-content">
            <p><strong>Name:</strong> ${salesOrderCreatedBy.firstName} ${salesOrderCreatedBy.lastName}</p>
            <p><strong>Email:</strong> ${salesOrderCreatedBy.loginName}</p>
            <p><strong>Role:</strong> ${salesOrderCreatedBy.role}</p>
            <p><strong>Phone:</strong> ${salesOrderCreatedBy.phone}</p>
        </div>
    </div>

    <!-- Purchase Order Created By -->
    <div class="section">
        <div class="section-title">Purchase Order Created By</div>
        <div class="section-content">
            <p><strong>Name:</strong> ${purchaseOrderCreatedBy.firstName} ${purchaseOrderCreatedBy.lastName}</p>
            <p><strong>Email:</strong> ${purchaseOrderCreatedBy.loginName}</p>
            <p><strong>Role:</strong> ${purchaseOrderCreatedBy.role}</p>
            <p><strong>Phone:</strong> ${purchaseOrderCreatedBy.phone}</p>
        </div>
    </div>

    <!-- Payment information -->
    <div class="section">
        <div class="section-title">Payment Information</div>
        <div class="section-content">
            <p><strong>Total:</strong> ${paymentInfo.total}</p>
            <p><strong>Tax:</strong> ${paymentInfo.tax}</p>
            <p><strong>Service Fee:</strong> ${paymentInfo.serviceFee}</p>
            <p><strong>Discount:</strong> ${paymentInfo.discount}</p>
            <p><strong>Status:</strong> ${paymentInfo.status}</p>
            <p><strong>Mode:</strong> ${paymentInfo.mode}</p>
            <p><strong>Subtotal:</strong> ${paymentInfo.subTotal}</p>
            <p><strong>Delivery Fee:</strong> ${paymentInfo.deliveryFee}</p>
            <p><strong>Razorpay Transaction ID:</strong> ${paymentInfo.razorpayTransactionId}</p>
            <p><strong>Razorpay Receipt:</strong> ${paymentInfo.razorpayReceipt}</p>
            <p><strong>Razorpay Order ID:</strong> ${paymentInfo.razorpayOrderId}</p>
            <p><strong>Razorpay Payment Notes:</strong> ${paymentInfo.razorpayPaymentNotes}</p>
        </div>
    </div>

    <!-- Product id, quantity, price mapping -->
    <div class="section">
        <div class="section-title">Product Details</div>
        <div class="section-content">
            <table class="table">
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Price Per Product</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                <#list salesOrdersProductQuantityMaps as map>
                    <tr>
                        <td>${map.productId}</td>
                        <td>${map.quantity}</td>
                        <td>${map.pricePerQuantityPerProduct}</td>
                        <td>${map.quantity*map.pricePerQuantityPerProduct}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
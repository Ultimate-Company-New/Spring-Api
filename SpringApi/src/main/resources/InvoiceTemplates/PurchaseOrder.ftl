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
            background-color: white !important;
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
            overflow: hidden; /* Clear floats */
            margin-bottom: 20px;
        }
        .profile_picture {
            float: left;
            width: 220px;
            height: 140px;
        }
        .company-info {
            float: right;
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
        <div class="profile_picture"></div>
        <div class="company-info">
            <h2>${companyName}</h2>
            <p>${website}</p>
            <p>${fullAddress}</p>
        </div>
    </div><hr />

    <h3>Purchase Order Invoice</h3>


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
            <#if purchaseOrder.orderReceipt?has_content>
                <p><strong>Order Receipt:</strong> ${purchaseOrder.orderReceipt}</p>
            </#if>
            <p><strong>Approved:</strong> ${purchaseOrder.approved?string('Yes', 'No')}</p>
        </div>
    </div>

    <!-- Shipping address -->
    <div class="section">
        <div class="section-title">Shipping Address</div>
        <div class="section-content address">
            <p>${shippingAddress.nameOnAddress}</p>
            <p>${shippingAddress.line1}, ${shippingAddress.line2}</p>
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
            <#if lead.website?exists && lead.website?has_content>
                <p><strong>Website:</strong> <a href="${lead.website}">${lead.website}</a></p>
            </#if>
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

    <!-- Purchase Order Approved By -->
    <div class="section">
        <div class="section-title">Purchase Order Approved By</div>
        <div class="section-content">
            <p><strong>Name:</strong> ${purchaseOrderApprovedBy.firstName} ${purchaseOrderApprovedBy.lastName}</p>
            <p><strong>Email:</strong> ${purchaseOrderApprovedBy.loginName}</p>
            <p><strong>Role:</strong> ${purchaseOrderApprovedBy.role}</p>
            <p><strong>Phone:</strong> ${purchaseOrderApprovedBy.phone}</p>
        </div>
    </div>

    <!-- Product id, quantity, price mapping -->
    <div class="section">
        <div class="section-title">Product Details</div>
        <div class="section-content">
            <table class="table">
                <thead>
                <tr>
                    <th>ProductId</th>
                    <th>Product Name</th>
                    <th>Quantity</th>
                </tr>
                </thead>
                <tbody>
                <#list purchaseOrdersProductQuantityMaps as product, quantity>
                    <tr>
                        <!-- Access the productId from the Product object -->
                        <td>${product.productId}</td>
                        <!-- Access the product title from the Product object -->
                        <td>${product.title}</td>
                        <!-- Access the quantity -->
                        <td>${quantity}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
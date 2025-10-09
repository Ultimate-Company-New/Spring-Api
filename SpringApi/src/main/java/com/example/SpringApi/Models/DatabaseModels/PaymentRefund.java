package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the PaymentRefund table.
 * 
 * This entity represents payment refund information and processing details.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PaymentRefund")
public class PaymentRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentRefundId", nullable = false)
    private Long paymentRefundId;

    @Column(name = "refundId", nullable = false)
    private String refundId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "refundType", nullable = false, length = 50)
    private String refundType;

    @Column(name = "refundStatus", nullable = false, length = 50)
    private String refundStatus;

    @Column(name = "refundMethod", nullable = false, length = 50)
    private String refundMethod;

    @Column(name = "speed", nullable = false, length = 50)
    private String speed;

    @Column(name = "approvedDate")
    private LocalDateTime approvedDate;

    @Column(name = "processedDate")
    private LocalDateTime processedDate;

    @Column(name = "completedDate")
    private LocalDateTime completedDate;

    @Column(name = "paymentId", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentId", insertable = false, updatable = false)
    private PaymentInfo payment;

    @Column(name = "razorpayId")
    private String razorpayId;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

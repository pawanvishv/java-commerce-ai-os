package com.commerceos.sellerservice.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.sellerservice.domain.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "seller_id", nullable = false, unique = true)
    private String sellerId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "pan")
    private String pan;

    @Column(name = "gstin")
    private String gstin;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_ifsc")
    private String bankIfsc;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SellerStatus status = SellerStatus.PENDING_KYC;

    @Column(name = "rating")
    @Builder.Default
    private BigDecimal rating = new BigDecimal("5.00");
}

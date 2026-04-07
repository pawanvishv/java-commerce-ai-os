package com.commerceos.platformadmin.domain.model;

import com.commerceos.common.model.BaseEntity;
import com.commerceos.platformadmin.domain.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "gstin")
    private String gstin;

    @Column(name = "pan")
    private String pan;

    @Column(name = "profile", nullable = false)
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClientStatus status;

    @Column(name = "plan")
    private String plan;
}
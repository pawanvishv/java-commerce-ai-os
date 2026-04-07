package com.commerceos.taxcalculation.domain.model;

import com.commerceos.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hsn_sac_slabs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsnSacSlab extends BaseEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "code_type", nullable = false)
    private String codeType;

    @Column(name = "description")
    private String description;

    @Column(name = "cgst_rate", nullable = false)
    private BigDecimal cgstRate;

    @Column(name = "sgst_rate", nullable = false)
    private BigDecimal sgstRate;

    @Column(name = "igst_rate", nullable = false)
    private BigDecimal igstRate;

    @Column(name = "cess_rate", nullable = false)
    private BigDecimal cessRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}

package com.commerceos.ledger.domain.model;

import com.commerceos.ledger.domain.enums.AccountType;
import com.commerceos.ledger.domain.enums.Direction;
import com.commerceos.ledger.domain.enums.EntryType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "entry_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "direction", nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "created_by")
    private String createdBy;
}

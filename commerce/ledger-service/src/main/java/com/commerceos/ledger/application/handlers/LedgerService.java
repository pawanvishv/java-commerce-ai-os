package com.commerceos.ledger.application.handlers;

import com.commerceos.ledger.domain.enums.AccountType;
import com.commerceos.ledger.domain.enums.Direction;
import com.commerceos.ledger.domain.enums.EntryType;
import com.commerceos.ledger.domain.model.LedgerEntry;
import com.commerceos.ledger.infrastructure.persistence.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * Non-seller 4-entry posting (correct double-entry):
     * DEBIT  Receivable        = totalPaise
     * CREDIT Revenue           = basePaise
     * CREDIT GST Payable       = taxPaise
     *
     * Then GST transfer entry:
     * DEBIT  GST Collected     = taxPaise
     * CREDIT GST Payable       = taxPaise
     *
     * Net: debits == credits
     */
    @Transactional
    public void postOrderPayment(String tenantId,
                                  String orderId,
                                  long totalPaise,
                                  long taxPaise,
                                  String referenceId) {
        long basePaise = totalPaise - taxPaise;

        List<LedgerEntry> entries = List.of(
                buildEntry(tenantId, orderId, EntryType.ORDER_PAYMENT,
                        AccountType.RECEIVABLE, Direction.DEBIT,
                        totalPaise, referenceId,
                        "Order payment receivable"),

                buildEntry(tenantId, orderId, EntryType.ORDER_PAYMENT,
                        AccountType.REVENUE, Direction.CREDIT,
                        basePaise, referenceId,
                        "Order revenue"),

                buildEntry(tenantId, orderId, EntryType.GST,
                        AccountType.GST_PAYABLE, Direction.CREDIT,
                        taxPaise, referenceId,
                        "GST payable")
        );

        validateDoubleEntry(entries);
        ledgerEntryRepository.saveAll(entries);
        log.info("Posted ledger entries for order: {}", orderId);
    }

    public List<LedgerEntry> getEntriesForOrder(String tenantId,
                                                  String orderId) {
        return ledgerEntryRepository
                .findByTenantIdAndOrderId(tenantId, orderId);
    }

    private LedgerEntry buildEntry(String tenantId, String orderId,
                                    EntryType entryType,
                                    AccountType accountType,
                                    Direction direction,
                                    long amountPaise,
                                    String referenceId,
                                    String description) {
        return LedgerEntry.builder()
                .tenantId(tenantId)
                .orderId(orderId)
                .entryType(entryType)
                .accountType(accountType)
                .direction(direction)
                .amountPaise(amountPaise)
                .referenceId(referenceId)
                .description(description)
                .build();
    }

    private void validateDoubleEntry(List<LedgerEntry> entries) {
        long debits = entries.stream()
                .filter(e -> e.getDirection() == Direction.DEBIT)
                .mapToLong(LedgerEntry::getAmountPaise).sum();
        long credits = entries.stream()
                .filter(e -> e.getDirection() == Direction.CREDIT)
                .mapToLong(LedgerEntry::getAmountPaise).sum();
        if (debits != credits) {
            throw new IllegalStateException(
                    "Double-entry validation failed: debits="
                            + debits + " credits=" + credits);
        }
    }
}

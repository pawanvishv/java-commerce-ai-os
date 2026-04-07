package com.commerceos.inventory.application.handlers;

import com.commerceos.inventory.domain.enums.MovementType;
import com.commerceos.inventory.domain.model.StoreInventory;
import com.commerceos.inventory.infrastructure.persistence.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrnService {

    private final StoreInventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    /**
     * GRN: Receive goods against a Purchase Order
     * Validates qty, adds to available stock
     */
    @Transactional
    public StoreInventory receiveGoods(String tenantId,
                                        String storeId,
                                        String sku,
                                        String poNumber,
                                        long receivedQty,
                                        long expectedQty) {
        log.info("GRN: receiving {} units of {} for PO: {}",
                receivedQty, sku, poNumber);

        if (receivedQty <= 0) {
            throw new IllegalArgumentException(
                    "Received qty must be positive");
        }

        if (receivedQty > expectedQty) {
            log.warn("GRN: over-receipt — received {} expected {}",
                    receivedQty, expectedQty);
        }

        if (receivedQty < expectedQty) {
            log.warn("GRN: short receipt — received {} expected {}",
                    receivedQty, expectedQty);
        }

        StoreInventory inventory = inventoryService
                .addStock(tenantId, storeId, sku, receivedQty);

        log.info("GRN completed: {} units of {} added to store {}",
                receivedQty, sku, storeId);
        return inventory;
    }

    /**
     * GRN: Receive damaged goods — goes to damaged_qty
     */
    @Transactional
    public StoreInventory receiveDamagedGoods(String tenantId,
                                               String storeId,
                                               String sku,
                                               long damagedQty) {
        log.info("GRN: receiving {} damaged units of {}",
                damagedQty, sku);

        StoreInventory inventory = inventoryRepository
                .findByTenantIdAndStoreIdAndSku(
                        tenantId, storeId, sku)
                .orElseGet(() -> StoreInventory.builder()
                        .tenantId(tenantId)
                        .storeId(storeId)
                        .sku(sku)
                        .build());

        inventory.setDamagedQty(
                inventory.getDamagedQty() + damagedQty);
        inventory = inventoryRepository.save(inventory);

        log.warn("GRN: {} damaged units of {} recorded",
                damagedQty, sku);
        return inventory;
    }
}

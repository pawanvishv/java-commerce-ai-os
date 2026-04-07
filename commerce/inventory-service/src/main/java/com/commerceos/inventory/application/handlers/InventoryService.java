package com.commerceos.inventory.application.handlers;

import com.commerceos.inventory.domain.enums.ReservationStatus;
import com.commerceos.inventory.domain.model.PendingReservation;
import com.commerceos.inventory.domain.model.StoreInventory;
import com.commerceos.inventory.infrastructure.persistence.PendingReservationRepository;
import com.commerceos.inventory.infrastructure.persistence.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StoreInventoryRepository inventoryRepository;
    private final PendingReservationRepository reservationRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String INVENTORY_KEY =
            "inventory:%s:%s:%s";
    private static final Duration RESERVATION_TTL =
            Duration.ofMinutes(15);

    @Transactional
    public StoreInventory addStock(String tenantId, String storeId,
                                    String sku, long qty) {
        StoreInventory inventory = inventoryRepository
                .findByTenantIdAndStoreIdAndSku(tenantId, storeId, sku)
                .orElseGet(() -> StoreInventory.builder()
                        .tenantId(tenantId)
                        .storeId(storeId)
                        .sku(sku)
                        .build());

        inventory.setAvailableQty(inventory.getAvailableQty() + qty);
        inventory = inventoryRepository.save(inventory);

        syncRedis(tenantId, storeId, sku,
                inventory.getAvailableQty());
        log.info("Added {} units of {} to store {}",
                qty, sku, storeId);
        return inventory;
    }

    @Transactional
    public PendingReservation reserve(String tenantId, String storeId,
                                       String sku, String orderId,
                                       long qty) {
        String redisKey = String.format(INVENTORY_KEY,
                tenantId, storeId, sku);

        Long remaining = redisTemplate.opsForValue()
                .decrement(redisKey, qty);

        if (remaining == null || remaining < 0) {
            if (remaining != null) {
                redisTemplate.opsForValue().increment(redisKey, qty);
            }
            throw new IllegalStateException(
                    "Insufficient inventory for SKU: " + sku);
        }

        PendingReservation reservation = PendingReservation.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .sku(sku)
                .orderId(orderId)
                .qty(qty)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().plus(RESERVATION_TTL))
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("Reserved {} units of {} for order {}",
                qty, sku, orderId);
        return reservation;
    }

    @Transactional
    public void confirmReservation(String tenantId, String storeId,
                                    String sku, String orderId) {
        reservationRepository
                .findByOrderIdAndSkuAndTenantId(orderId, sku, tenantId)
                .ifPresent(reservation -> {
                    StoreInventory inventory = inventoryRepository
                            .findByTenantIdAndStoreIdAndSkuForUpdate(
                                    tenantId, storeId, sku)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Inventory not found"));

                    inventory.setAvailableQty(
                            inventory.getAvailableQty()
                                    - reservation.getQty());
                    inventory.setReservedQty(
                            inventory.getReservedQty()
                                    + reservation.getQty());

                    inventoryRepository.save(inventory);
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    reservationRepository.save(reservation);
                    log.info("Confirmed reservation for order: {}",
                            orderId);
                });
    }

    public StoreInventory getInventory(String tenantId,
                                        String storeId,
                                        String sku) {
        return inventoryRepository
                .findByTenantIdAndStoreIdAndSku(
                        tenantId, storeId, sku)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory not found for SKU: " + sku));
    }

    private void syncRedis(String tenantId, String storeId,
                            String sku, long qty) {
        String key = String.format(INVENTORY_KEY,
                tenantId, storeId, sku);
        redisTemplate.opsForValue().set(key, String.valueOf(qty));
    }
}

package com.commerceos.orderservice.application.handlers;

import com.commerceos.orderservice.application.commands.CreateOrderCommand;
import com.commerceos.orderservice.domain.enums.ItemType;
import com.commerceos.orderservice.domain.enums.OrderChannel;
import com.commerceos.orderservice.domain.enums.OrderStatus;
import com.commerceos.orderservice.domain.model.Order;
import com.commerceos.orderservice.domain.model.OrderLine;
import com.commerceos.orderservice.domain.model.SagaState;
import com.commerceos.orderservice.infrastructure.persistence.OrderRepository;
import com.commerceos.orderservice.infrastructure.persistence.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SagaStateRepository sagaStateRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Order createOrder(CreateOrderCommand cmd,
                              String idempotencyKey) {
        String orderNumber = "ORD-" + System.currentTimeMillis();

        Order order = Order.builder()
                .tenantId(cmd.getTenantId())
                .orderNumber(orderNumber)
                .customerId(cmd.getCustomerId())
                .storeId(cmd.getStoreId())
                .status(OrderStatus.PENDING)
                .channel(cmd.getChannel() != null
                        ? OrderChannel.valueOf(cmd.getChannel())
                        : OrderChannel.ONLINE)
                .notes(cmd.getNotes())
                .build();

        long totalPaise = 0;
        long taxPaise = 0;

        for (CreateOrderCommand.OrderLineCommand lineCmd
                : cmd.getLines()) {
            long lineTax = lineCmd.getTaxPaise();
            long lineTotal = (lineCmd.getUnitPricePaise()
                    * lineCmd.getQty()) + lineTax;

            OrderLine line = OrderLine.builder()
                    .order(order)
                    .tenantId(cmd.getTenantId())
                    .sku(lineCmd.getSku())
                    .itemName(lineCmd.getItemName())
                    .itemType(ItemType.valueOf(
                            lineCmd.getItemType() != null
                                    ? lineCmd.getItemType()
                                    : "PHYSICAL"))
                    .qty(lineCmd.getQty())
                    .unitPricePaise(lineCmd.getUnitPricePaise())
                    .taxPaise(lineTax)
                    .totalPaise(lineTotal)
                    .build();

            order.getLines().add(line);
            totalPaise += lineTotal;
            taxPaise += lineTax;
        }

        order.setTotalPaise(totalPaise);
        order.setTaxPaise(taxPaise);
        order = orderRepository.save(order);

        SagaState saga = SagaState.builder()
                .orderId(order.getId())
                .tenantId(cmd.getTenantId())
                .status("STARTED")
                .currentStep("ORDER_CREATED")
                .build();
        sagaStateRepository.save(saga);

        publishEvent("order.created.v1", cmd.getTenantId(),
                Map.of(
                        "orderId", order.getId().toString(),
                        "tenantId", cmd.getTenantId(),
                        "orderNumber", orderNumber,
                        "totalPaise", totalPaise
                ));

        log.info("Order created: {} for tenant: {}",
                orderNumber, cmd.getTenantId());
        return order;
    }

    public Order getOrder(String orderNumber, String tenantId) {
        return orderRepository
                .findByOrderNumberAndTenantId(orderNumber, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderNumber));
    }

    public List<Order> getOrdersByStatus(
            String tenantId, OrderStatus status) {
        return orderRepository
                .findByTenantIdAndStatus(tenantId, status);
    }

    @Transactional
    public Order updateStatus(String orderNumber,
                               String tenantId,
                               OrderStatus newStatus) {
        Order order = getOrder(orderNumber, tenantId);
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        if (newStatus == OrderStatus.PAID) {
            publishEvent("order.paid.v1", tenantId,
                    Map.of(
                            "orderId", order.getId().toString(),
                            "tenantId", tenantId,
                            "orderNumber", orderNumber,
                            "totalPaise", order.getTotalPaise(),
                            "taxPaise", order.getTaxPaise()
                    ));

            publishEvent("notification.send.v1", tenantId,
                    Map.of(
                            "tenantId", tenantId,
                            "payload", Map.of(
                                    "recipient", tenantId
                                            + "@commerce.local",
                                    "channel", "EMAIL",
                                    "templateCode", "ORDER_PAID",
                                    "subject", "Order Confirmed: "
                                            + orderNumber,
                                    "body", "Your order "
                                            + orderNumber
                                            + " has been confirmed."
                            )
                    ));
        }

        log.info("Order {} status updated to: {}",
                orderNumber, newStatus);
        return order;
    }

    private void publishEvent(String topic, String key,
                               Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
            log.debug("Published event to: {}", topic);
        } catch (Exception e) {
            log.warn("Failed to publish to {}: {}",
                    topic, e.getMessage());
        }
    }
}

package com.commerceos.orderservice.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.orderservice.application.commands.CreateOrderCommand;
import com.commerceos.orderservice.application.handlers.OrderService;
import com.commerceos.orderservice.domain.enums.OrderStatus;
import com.commerceos.orderservice.domain.model.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody CreateOrderCommand cmd) {

        cmd = CreateOrderCommand.builder()
                .tenantId(tenantId)
                .customerId(cmd.getCustomerId())
                .storeId(cmd.getStoreId())
                .channel(cmd.getChannel())
                .notes(cmd.getNotes())
                .lines(cmd.getLines())
                .build();

        Order order = orderService.createOrder(cmd, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(order, "Order created"));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable String orderNumber,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Order order = orderService.getOrder(orderNumber, tenantId);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getOrders(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(required = false,
                    defaultValue = "PENDING") String status) {

        List<Order> orders = orderService.getOrdersByStatus(
                tenantId, OrderStatus.valueOf(status));
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable String orderNumber,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String status) {

        Order order = orderService.updateStatus(
                orderNumber, tenantId, OrderStatus.valueOf(status));
        return ResponseEntity.ok(ApiResponse.ok(order,
                "Order status updated"));
    }
}

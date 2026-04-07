package com.commerceos.orderservice.infrastructure.persistence;

import com.commerceos.orderservice.domain.enums.OrderStatus;
import com.commerceos.orderservice.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumberAndTenantId(
            String orderNumber, String tenantId);

    List<Order> findByTenantIdAndStatus(
            String tenantId, OrderStatus status);

    List<Order> findByCustomerIdAndTenantId(
            String customerId, String tenantId);
}

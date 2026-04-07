package com.commerceos.paymentadapter.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "payment.gateways.razorpay.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RazorpayAdapter implements PaymentGatewayAdapter {

    @Override
    public String createOrder(String orderId, long amountPaise,
                               String currency,
                               Map<String, String> metadata) {
        log.info("Razorpay: creating order for {} amount {}",
                orderId, amountPaise);
        return "rzp_order_" + UUID.randomUUID().toString()
                .substring(0, 8);
    }

    @Override
    public Map<String, Object> capturePayment(String gatewayOrderId,
                                               String gatewayPaymentId) {
        log.info("Razorpay: capturing payment {}", gatewayPaymentId);
        return Map.of(
                "status", "captured",
                "id", gatewayPaymentId,
                "order_id", gatewayOrderId);
    }

    @Override
    public Map<String, Object> refund(String gatewayPaymentId,
                                       long amountPaise) {
        log.info("Razorpay: refunding {} for payment {}",
                amountPaise, gatewayPaymentId);
        return Map.of(
                "id", "rfnd_" + UUID.randomUUID().toString()
                        .substring(0, 8),
                "status", "processed");
    }

    @Override
    public Map<String, Object> getStatus(String gatewayOrderId) {
        return Map.of("status", "created",
                "id", gatewayOrderId);
    }

    @Override
    public String getGatewayName() {
        return "RAZORPAY";
    }
}

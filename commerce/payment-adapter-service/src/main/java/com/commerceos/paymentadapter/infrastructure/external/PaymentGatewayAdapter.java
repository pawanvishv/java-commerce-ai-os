package com.commerceos.paymentadapter.infrastructure.external;

import java.util.Map;

public interface PaymentGatewayAdapter {

    String createOrder(String orderId, long amountPaise,
                       String currency, Map<String, String> metadata);

    Map<String, Object> capturePayment(String gatewayOrderId,
                                        String gatewayPaymentId);

    Map<String, Object> refund(String gatewayPaymentId,
                                long amountPaise);

    Map<String, Object> getStatus(String gatewayOrderId);

    String getGatewayName();
}

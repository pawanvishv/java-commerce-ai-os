package com.commerceos.paymentadapter.application.handlers;

import com.commerceos.kafka.contracts.Topics;
import com.commerceos.paymentadapter.domain.enums.PaymentGateway;
import com.commerceos.paymentadapter.domain.enums.TenderStatus;
import com.commerceos.paymentadapter.domain.model.PaymentTender;
import com.commerceos.paymentadapter.infrastructure.external.PaymentGatewayAdapter;
import com.commerceos.paymentadapter.infrastructure.persistence.PaymentTenderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PaymentService {

    private final PaymentTenderRepository tenderRepository;
    private final List<PaymentGatewayAdapter> gatewayAdapters;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentTender capturePayment(
            String idempotencyKey,
            String orderId,
            String tenantId,
            long amountPaise,
            String gatewayName,
            String gatewayOrderId,
            String gatewayPaymentId) {

        return tenderRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> {
                    PaymentGatewayAdapter adapter = findAdapter(gatewayName);
                    Map<String, Object> result = adapter.capturePayment(
                            gatewayOrderId, gatewayPaymentId);

                    PaymentTender tender = PaymentTender.builder()
                            .idempotencyKey(idempotencyKey)
                            .orderId(orderId)
                            .tenantId(tenantId)
                            .gateway(PaymentGateway.valueOf(
                                    gatewayName.toUpperCase()))
                            .gatewayOrderId(gatewayOrderId)
                            .gatewayPaymentId(gatewayPaymentId)
                            .amountPaise(amountPaise)
                            .status(TenderStatus.CAPTURED)
                            .build();

                    try {
                        tender.setRawResponse(
                                objectMapper.writeValueAsString(result));
                    } catch (Exception e) {
                        log.warn("Could not serialize gateway response");
                    }

                    tender = tenderRepository.save(tender);

                    kafkaTemplate.send(Topics.PAYMENT_CAPTURED,
                            tenantId,
                            Map.of(
                                    "orderId", orderId,
                                    "tenantId", tenantId,
                                    "amountPaise", amountPaise,
                                    "gatewayPaymentId", gatewayPaymentId
                            ));

                    log.info("Payment captured for order: {}", orderId);
                    return tender;
                });
    }

    public List<PaymentTender> getTendersByOrder(
            String orderId, String tenantId) {
        return tenderRepository.findByOrderIdAndTenantId(
                orderId, tenantId);
    }

    private PaymentGatewayAdapter findAdapter(String gatewayName) {
        return gatewayAdapters.stream()
                .filter(a -> a.getGatewayName()
                        .equalsIgnoreCase(gatewayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No adapter found for gateway: " + gatewayName));
    }
}

package com.commerceos.paymentadapter.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.paymentadapter.application.handlers.PaymentService;
import com.commerceos.paymentadapter.domain.model.PaymentTender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<PaymentTender>> capture(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String orderId,
            @RequestParam long amountPaise,
            @RequestParam String gateway,
            @RequestParam String gatewayOrderId,
            @RequestParam String gatewayPaymentId) {

        PaymentTender tender = paymentService.capturePayment(
                idempotencyKey, orderId, tenantId,
                amountPaise, gateway,
                gatewayOrderId, gatewayPaymentId);

        return ResponseEntity.ok(ApiResponse.ok(tender,
                "Payment captured"));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentTender>>> getByOrder(
            @PathVariable String orderId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        List<PaymentTender> tenders = paymentService
                .getTendersByOrder(orderId, tenantId);
        return ResponseEntity.ok(ApiResponse.ok(tenders));
    }
}

package com.commerceos.provisioning.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.provisioning.application.handlers.ProvisioningService;
import com.commerceos.provisioning.domain.model.ProvisioningJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/provisioning")
@RequiredArgsConstructor
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    @PostMapping("/tenants/{tenantId}")
    public ResponseEntity<ApiResponse<ProvisioningJob>> provision(
            @PathVariable String tenantId,
            @RequestParam String profile) {

        ProvisioningJob job = provisioningService
                .startProvisioning(tenantId, profile);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(job,
                        "Provisioning started"));
    }

    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<ApiResponse<ProvisioningJob>> getStatus(
            @PathVariable String tenantId) {

        ProvisioningJob job = provisioningService.getJob(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }
}

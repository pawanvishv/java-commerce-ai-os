package com.commerceos.platformadmin.api.rest;

import com.commerceos.common.model.ApiResponse;
import com.commerceos.platformadmin.application.commands.RegisterClientCommand;
import com.commerceos.platformadmin.application.handlers.ClientService;
import com.commerceos.platformadmin.domain.enums.ClientStatus;
import com.commerceos.platformadmin.domain.model.Client;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse<Client>> register(
            @Valid @RequestBody RegisterClientCommand cmd) {

        Client client = clientService.registerClient(cmd);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(client, "Client registered successfully"));
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<Client>> getClient(
            @PathVariable String tenantId) {

        Client client = clientService.findByTenantId(tenantId);

        return ResponseEntity.ok(ApiResponse.ok(client));
    }

    @PatchMapping("/{tenantId}/status")
    public ResponseEntity<ApiResponse<Client>> updateStatus(
            @PathVariable String tenantId,
            @RequestParam ClientStatus status) {

        Client client = clientService.updateStatus(tenantId, status);

        return ResponseEntity.ok(
                ApiResponse.ok(client, "Status updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Client>>> listByStatus(
            @RequestParam(required = false) ClientStatus status) {

        List<Client> clients = (status != null)
                ? clientService.findByStatus(status)
                : clientService.findByStatus(ClientStatus.ACTIVE);

        return ResponseEntity.ok(ApiResponse.ok(clients));
    }
}
package com.commerceos.platformadmin.application.handlers;

import com.commerceos.common.util.SlugUtils;
import com.commerceos.platformadmin.application.commands.RegisterClientCommand;
import com.commerceos.platformadmin.domain.enums.ClientStatus;
import com.commerceos.platformadmin.domain.model.Client;
import com.commerceos.platformadmin.domain.model.OutboxEvent;
import com.commerceos.platformadmin.infrastructure.persistence.ClientRepository;
import com.commerceos.platformadmin.infrastructure.persistence.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Client registerClient(RegisterClientCommand cmd) {

        if (clientRepository.existsByEmail(cmd.getEmail())) {
            throw new IllegalArgumentException(
                    "Client already exists with email: " + cmd.getEmail());
        }

        String tenantId = UUID.randomUUID().toString();
        String slug = SlugUtils.toSlug(cmd.getBusinessName())
                + "-" + tenantId.substring(0, 8);

        Client client = Client.builder()
                .tenantId(tenantId)
                .slug(slug)
                .businessName(cmd.getBusinessName())
                .email(cmd.getEmail())
                .phone(cmd.getPhone())
                .gstin(cmd.getGstin())
                .pan(cmd.getPan())
                .profile(cmd.getProfile())
                .status(ClientStatus.UNVERIFIED)
                .build();

        client = clientRepository.save(client);

        log.info("Client registered: businessName={}, tenantId={}",
                client.getBusinessName(), tenantId);

        publishOutboxEvent(
                "CLIENT",
                tenantId,
                "client.registered.v1",
                client
        );

        return client;
    }

    @Transactional
    public Client updateStatus(String tenantId, ClientStatus newStatus) {

        Client client = clientRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found: " + tenantId));

        ClientStatus oldStatus = client.getStatus();
        client.setStatus(newStatus);

        client = clientRepository.save(client);

        log.info("Client status updated: tenantId={}, oldStatus={}, newStatus={}",
                tenantId, oldStatus, newStatus);

        publishOutboxEvent(
                "CLIENT",
                tenantId,
                "client.status.changed.v1",
                client
        );

        return client;
    }

    public List<Client> findByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status);
    }

    public Client findByTenantId(String tenantId) {
        return clientRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found: " + tenantId));
    }

    private void publishOutboxEvent(String aggregateType,
                                    String aggregateId,
                                    String eventType,
                                    Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .build();

            outboxEventRepository.save(event);

        } catch (Exception e) {
            log.error("Failed to publish outbox event", e);
        }
    }
}
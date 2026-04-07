package com.commerceos.notification.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationCommand {

    private String tenantId;
    private String templateCode;
    private String recipient;
    private String channel;
    private Map<String, String> variables;
    private String subject;
    private String body;
}

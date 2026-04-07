package com.commerceos.orderservice.application.commands;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {

    private String tenantId;
    private String customerId;
    private String storeId;
    private String channel;
    private String notes;

    @NotEmpty
    private List<OrderLineCommand> lines;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineCommand {
        private String sku;
        private String itemName;
        private String itemType;
        private int qty;
        private long unitPricePaise;
        private long taxPaise;
    }
}

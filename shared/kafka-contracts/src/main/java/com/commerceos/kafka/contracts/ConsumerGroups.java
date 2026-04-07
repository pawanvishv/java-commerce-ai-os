package com.commerceos.kafka.contracts;

/**
 * Single source of truth for all Kafka consumer group IDs.
 */
public final class ConsumerGroups {

    private ConsumerGroups() {}

    public static final String LEDGER_SERVICE          = "ledger-service";
    public static final String NOTIFICATION_SERVICE    = "notification-service";
    public static final String INVENTORY_SERVICE       = "inventory-service";
    public static final String CATALOG_INDEXER         = "catalog-opensearch-indexer";
    public static final String ORDER_SERVICE           = "order-service";
    public static final String BILLING_SERVICE         = "billing-service";
    public static final String SELLER_SERVICE          = "seller-service";
    public static final String PAYMENT_ADAPTER         = "payment-adapter-service";
}

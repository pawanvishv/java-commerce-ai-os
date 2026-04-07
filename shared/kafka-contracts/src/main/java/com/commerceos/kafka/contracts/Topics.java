package com.commerceos.kafka.contracts;

public final class Topics {

    private Topics() {}

    public static final String CATALOG_ITEM_UPDATED  = "catalog.item.updated.v1";
    public static final String ORDER_PAID            = "order.paid.v1";
    public static final String PAYMENT_CAPTURED      = "payment.captured.v1";
    public static final String DELIVERY_COMPLETED    = "delivery.completed.v1";
    public static final String SERVICE_COMPLETED     = "service.completed.v1";
    public static final String INVENTORY_UPDATED     = "inventory.updated.v1";
    public static final String INVENTORY_LOW_STOCK   = "inventory.low.stock.v1";
    public static final String NOTIFICATION_SEND     = "notification.send.v1";
    public static final String SELLER_SALE           = "seller.sale.v1";
    public static final String SELLER_KYC_COMPLETED  = "seller.kyc.completed.v1";
    public static final String TENANT_PROVISIONED    = "tenant.provisioned.v1";
    public static final String TENANT_SUSPENDED      = "tenant.suspended.v1";
    public static final String TENANT_UPGRADED       = "tenant.upgraded.v1";
    public static final String DLQ_SUFFIX            = ".dlq";

    public static String dlq(String topic) {
        return topic + DLQ_SUFFIX;
    }
}

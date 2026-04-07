# Commerce OS — Complete API Documentation

> Java 21 + Spring Boot | 13 Services | Multi-Tenant Commerce Platform

---

## Table of Contents

1. [Authentication Flow](#1-authentication-flow)
2. [API Gateway](#2-api-gateway)
3. [Platform Admin Service](#3-platform-admin-service)
4. [Provisioning Service](#4-provisioning-service)
5. [Notification Service](#5-notification-service)
6. [Tax Calculation Service](#6-tax-calculation-service)
7. [Payment Adapter Service](#7-payment-adapter-service)
8. [Ledger Service](#8-ledger-service)
9. [Inventory Service](#9-inventory-service)
10. [Catalog Service](#10-catalog-service)
11. [Order Service](#11-order-service)
12. [Seller Service](#12-seller-service)
13. [End-to-End Flow](#13-end-to-end-flow)
14. [Kafka Event Flows](#14-kafka-event-flows)
15. [Common Headers](#15-common-headers)
16. [Error Responses](#16-error-responses)


---

## Base URLs

| Service | Base URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Auth Service | http://localhost:8081 |
| Platform Admin | http://localhost:8082 |
| Provisioning | http://localhost:8083 |
| Notification | http://localhost:8084 |
| Tax Calculation | http://localhost:8085 |
| Payment Adapter | http://localhost:8086 |
| Ledger | http://localhost:8087 |
| Inventory | http://localhost:8089 |
| Catalog | http://localhost:8091 |
| Order | http://localhost:8092 |
| Seller | http://localhost:8093 |

---

## 1. Authentication Flow

### Service: `auth-service` — Port 8081

All API calls require a JWT token obtained from auth-service.

---

#### 1.1 Get JWT Token

```
POST /api/v1/auth/token
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| clientId | string | yes | API client ID |
| clientSecret | string | yes | API client secret |

**Example Request:**
```bash
curl -X POST "http://localhost:8081/api/v1/auth/token?clientId=test-client-001&clientSecret=secret123"
```

**Example Response:**
```json
{
    "access_token": "eyJraWQiOiJjb21tZXJjZS1vcy1rZXktMSIsImFsZyI6IlJTMjU2In0...",
    "token_type": "Bearer"
}
```

---

#### 1.2 Get JWKS Public Key

```
GET /.well-known/jwks.json
```

**Example Response:**
```json
{
    "keys": [
        {
            "kty": "RSA",
            "e": "AQAB",
            "kid": "commerce-os-key-1",
            "n": "qjU-YPsvn_Og4RFFoqkO0q3RN..."
        }
    ]
}
```

---

#### 1.3 Generate BCrypt Hash (Dev only)

```
GET /api/v1/auth/hash?secret={secret}
```

**Example:**
```bash
curl "http://localhost:8081/api/v1/auth/hash?secret=mypassword"
```

---

## 2. API Gateway

### Service: `api-gateway` — Port 8080

All downstream requests go through the gateway. Gateway handles:
- JWT validation
- Tenant resolution
- Rate limiting (100 req/sec per tenant)
- Injects `X-Tenant-ID`, `X-Schema-Name`, `X-Tenant-Profile` headers

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

**Authenticated Request Example:**
```bash
curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer eyJ..."
```

---

## 3. Platform Admin Service

### Service: `platform-admin-service` — Port 8082

Manages client/tenant lifecycle.

---

#### 3.1 Register Client (Tenant)

```
POST /api/v1/admin/clients
Content-Type: application/json
```

**Request Body:**
```json
{
    "businessName": "TechCorp India",
    "email": "admin@techcorp.in",
    "phone": "9999999999",
    "gstin": "29ABCDE1234F1Z5",
    "pan": "ABCDE1234F",
    "profile": "FULL_COMMERCE",
    "plan": "PROFESSIONAL"
}
```

**Profiles:** `BILLING_ONLY` | `INVENTORY_ONLY` | `POS_ONLY` | `POS_BILLING` | `WEB_COMMERCE` | `FULL_COMMERCE`

**Example Response:**
```json
{
    "success": true,
    "message": "Client registered successfully",
    "data": {
        "id": "dfd5f794-a7a4-45a4-97fa-df2f556257eb",
        "tenantId": "e1cf3f42-c727-45e6-a6a3-c1c808d55192",
        "slug": "techcorp-india-e1cf3f42",
        "businessName": "TechCorp India",
        "status": "UNVERIFIED",
        "profile": "FULL_COMMERCE"
    }
}
```

---

#### 3.2 Get Client by Tenant ID

```
GET /api/v1/admin/clients/{tenantId}
```

**Example:**
```bash
curl "http://localhost:8082/api/v1/admin/clients/e1cf3f42-c727-45e6-a6a3-c1c808d55192"
```

---

#### 3.3 Update Client Status

```
PATCH /api/v1/admin/clients/{tenantId}/status?status={status}
```

**Status Flow:**
```
UNVERIFIED → VERIFIED → PAYMENT_DONE → ACTIVE → SUSPENDED → TERMINATED
```

**Example:**
```bash
curl -X PATCH "http://localhost:8082/api/v1/admin/clients/tenant-001/status?status=ACTIVE"
```

---

#### 3.4 List Clients by Status

```
GET /api/v1/admin/clients?status={status}
```

**Example:**
```bash
curl "http://localhost:8082/api/v1/admin/clients?status=ACTIVE"
```

---

## 4. Provisioning Service

### Service: `provisioning-service` — Port 8083

Handles tenant provisioning via Temporal workflows.

---

#### 4.1 Start Provisioning

```
POST /api/v1/provisioning/tenants/{tenantId}?profile={profile}
```

**Example:**
```bash
curl -X POST "http://localhost:8083/api/v1/provisioning/tenants/tenant-001?profile=FULL_COMMERCE"
```

**Response:**
```json
{
    "success": true,
    "message": "Provisioning started",
    "data": {
        "tenantId": "tenant-001",
        "status": "PENDING",
        "workflowId": "provision-tenant-001"
    }
}
```

---

#### 4.2 Get Provisioning Status

```
GET /api/v1/provisioning/tenants/{tenantId}
```

**Example:**
```bash
curl "http://localhost:8083/api/v1/provisioning/tenants/tenant-001"
```

**Status Values:** `PENDING` | `IN_PROGRESS` | `COMPLETED` | `FAILED`

---

## 5. Notification Service

### Service: `notification-service` — Port 8084

Consumes `notification.send.v1` Kafka events. No direct REST API — event-driven only.

**Health Check:**
```bash
curl http://localhost:8084/actuator/health
```

**Kafka Event Format** (publish to `notification.send.v1`):
```json
{
    "tenantId": "tenant-001",
    "payload": {
        "recipient": "user@example.com",
        "channel": "EMAIL",
        "templateCode": "ORDER_PAID",
        "subject": "Order Confirmed",
        "body": "Your order has been confirmed."
    }
}
```

**Template Codes:**
- `CLIENT_REGISTERED` — Welcome email
- `TENANT_ACTIVATED` — Account active
- `TENANT_SUSPENDED` — Account suspended
- `ORDER_PAID` — Order confirmation

---

## 6. Tax Calculation Service

### Service: `tax-calculation-service` — Port 8085

Stateless GST calculation. No DB writes.

---

#### 6.1 Calculate Tax

```
POST /api/v1/tax/calculate
Content-Type: application/json
```

**Request Body:**
```json
{
    "basePricePaise": 10000,
    "taxMode": "TAX_EXCLUSIVE",
    "taxCodeType": "HSN",
    "taxCode": "8471",
    "qty": 1,
    "uom": "PCS"
}
```

**Fields:**

| Field | Type | Description |
|---|---|---|
| basePricePaise | long | Price in paise (1 rupee = 100 paise) |
| taxMode | string | `TAX_INCLUSIVE` or `TAX_EXCLUSIVE` |
| taxCodeType | string | `HSN` or `SAC` |
| taxCode | string | HSN/SAC code |
| qty | int | Quantity |
| uom | string | Unit of measure |

**Example Response:**
```json
{
    "success": true,
    "data": {
        "basePricePaise": 10000,
        "totalTaxPaise": 1800,
        "totalPaise": 11800,
        "displayMode": "TAX_EXCLUSIVE",
        "taxBreakdown": [
            {"taxType": "CGST", "rate": 9.00, "amountPaise": 900},
            {"taxType": "SGST", "rate": 9.00, "amountPaise": 900}
        ]
    }
}
```

**Common HSN Codes:**

| HSN | Description | GST Rate |
|---|---|---|
| 8471 | Computers | 18% |
| 8517 | Mobile phones | 12% |
| 6101 | Men's overcoats | 5% |
| 1001 | Wheat | 0% |

**Common SAC Codes:**

| SAC | Description | GST Rate |
|---|---|---|
| 9983 | IT services | 18% |
| 9984 | Telecom services | 18% |
| 9987 | Maintenance services | 18% |

---

## 7. Payment Adapter Service

### Service: `payment-adapter-service` — Port 8086

Unified interface over Razorpay, Stripe, PhonePe.

---

#### 7.1 Capture Payment

```
POST /api/v1/payments/capture
```

**Required Headers:**
- `Idempotency-Key` — unique key per request
- `X-Tenant-ID` — tenant identifier

**Query Parameters:**

| Parameter | Type | Required |
|---|---|---|
| orderId | string | yes |
| amountPaise | long | yes |
| gateway | string | yes |
| gatewayOrderId | string | yes |
| gatewayPaymentId | string | yes |

**Example:**
```bash
curl -X POST "http://localhost:8086/api/v1/payments/capture?orderId=order-001&amountPaise=11800&gateway=RAZORPAY&gatewayOrderId=rzp_order_001&gatewayPaymentId=rzp_pay_001" \
  -H "Idempotency-Key: idem-001" \
  -H "X-Tenant-ID: tenant-001"
```

**Response:**
```json
{
    "success": true,
    "message": "Payment captured",
    "data": {
        "id": "uuid",
        "orderId": "order-001",
        "amountPaise": 11800,
        "status": "CAPTURED",
        "gateway": "RAZORPAY"
    }
}
```

**Supported Gateways:** `RAZORPAY` | `STRIPE` | `PHONEPE`

**Payment Status Flow:**
```
PENDING → CAPTURED → REFUNDED
       → FAILED
```

---

#### 7.2 Get Tenders by Order

```
GET /api/v1/payments/orders/{orderId}
```

**Required Headers:** `X-Tenant-ID`

---

## 8. Ledger Service

### Service: `ledger-service` — Port 8087

Append-only double-entry bookkeeping. No updates or deletes allowed.

---

#### 8.1 Post Order Payment (Manual)

```
POST /api/v1/ledger/orders/{orderId}/post?totalPaise={total}&taxPaise={tax}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8087/api/v1/ledger/orders/order-001/post?totalPaise=11800&taxPaise=1800" \
  -H "X-Tenant-ID: tenant-001"
```

**Auto-triggered by:** `payment.captured.v1` and `order.paid.v1` Kafka events.

---

#### 8.2 Get Ledger Entries for Order

```
GET /api/v1/ledger/orders/{orderId}
```

**Required Headers:** `X-Tenant-ID`

**Example Response:**
```json
{
    "success": true,
    "data": [
        {
            "accountType": "RECEIVABLE",
            "direction": "DEBIT",
            "amountPaise": 11800,
            "description": "Order payment receivable"
        },
        {
            "accountType": "REVENUE",
            "direction": "CREDIT",
            "amountPaise": 10000,
            "description": "Order revenue"
        },
        {
            "accountType": "GST_PAYABLE",
            "direction": "CREDIT",
            "amountPaise": 1800,
            "description": "GST payable"
        }
    ]
}
```

**Account Types:**
- `RECEIVABLE` — Amount due from customer
- `REVENUE` — Net revenue (base price)
- `GST_COLLECTED` — GST collected
- `GST_PAYABLE` — GST payable to government
- `COMMISSION_REVENUE` — Platform commission
- `TCS_PAYABLE` — TCS deducted at source
- `SELLER_NET_PAYABLE` — Amount payable to seller

---

## 9. Inventory Service

### Service: `inventory-service` — Port 8089

Stock management with Redis-backed atomic reservations.

---

#### 9.1 Add Stock

```
POST /api/v1/inventory/stores/{storeId}/skus/{sku}/stock?qty={qty}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8089/api/v1/inventory/stores/store-001/skus/LAPTOP-001/stock?qty=100" \
  -H "X-Tenant-ID: tenant-001"
```

**Response:**
```json
{
    "success": true,
    "message": "Stock added successfully",
    "data": {
        "tenantId": "tenant-001",
        "storeId": "store-001",
        "sku": "LAPTOP-001",
        "availableQty": 100,
        "reservedQty": 0,
        "damagedQty": 0
    }
}
```

---

#### 9.2 Get Inventory

```
GET /api/v1/inventory/stores/{storeId}/skus/{sku}
```

**Required Headers:** `X-Tenant-ID`

---

#### 9.3 Reserve Stock

```
POST /api/v1/inventory/stores/{storeId}/skus/{sku}/reserve?orderId={orderId}&qty={qty}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8089/api/v1/inventory/stores/store-001/skus/LAPTOP-001/reserve?orderId=order-001&qty=2" \
  -H "X-Tenant-ID: tenant-001"
```

**Reservation expires in 15 minutes if not confirmed.**

---

#### 9.4 Confirm Reservation

```
POST /api/v1/inventory/stores/{storeId}/skus/{sku}/confirm?orderId={orderId}
```

**Required Headers:** `X-Tenant-ID`

---

#### 9.5 GRN — Receive Goods

```
POST /api/v1/inventory/grn/receive?storeId={storeId}&sku={sku}&poNumber={po}&receivedQty={qty}&expectedQty={qty}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8089/api/v1/inventory/grn/receive?storeId=store-001&sku=LAPTOP-001&poNumber=PO-001&receivedQty=50&expectedQty=50" \
  -H "X-Tenant-ID: tenant-001"
```

---

#### 9.6 GRN — Record Damaged Goods

```
POST /api/v1/inventory/grn/damaged?storeId={storeId}&sku={sku}&damagedQty={qty}
```

**Required Headers:** `X-Tenant-ID`

---

## 10. Catalog Service

### Service: `catalog-service` — Port 8091

MongoDB-backed product catalog with Kafka event publishing.

---

#### 10.1 Create Catalog Item

```
POST /api/v1/catalog/items
Content-Type: application/json
```

**Required Headers:** `X-Tenant-ID`

**Request Body:**
```json
{
    "sku": "LAPTOP-001",
    "name": "Business Laptop",
    "description": "15 inch business laptop",
    "itemType": "PHYSICAL",
    "basePricePaise": 5000000,
    "taxCode": "8471",
    "taxCodeType": "HSN",
    "uom": "PCS",
    "tags": ["laptop", "electronics"],
    "attributes": {
        "brand": "Dell",
        "ram": "16GB",
        "storage": "512GB SSD"
    }
}
```

**Item Types:** `PHYSICAL` | `SERVICE` | `DIGITAL` | `BUNDLE`

**Example Response:**
```json
{
    "success": true,
    "message": "Item created",
    "data": {
        "id": "60b8d6f4e1d2a3b4c5d6e7f8",
        "tenantId": "tenant-001",
        "sku": "LAPTOP-001",
        "name": "Business Laptop",
        "itemType": "PHYSICAL",
        "basePricePaise": 5000000,
        "status": "ACTIVE"
    }
}
```

---

#### 10.2 Get All Active Items

```
GET /api/v1/catalog/items
```

**Required Headers:** `X-Tenant-ID`

---

#### 10.3 Get Item by SKU

```
GET /api/v1/catalog/items/sku/{sku}
```

**Required Headers:** `X-Tenant-ID`

---

#### 10.4 Update Item

```
PUT /api/v1/catalog/items/{id}
Content-Type: application/json
```

**Required Headers:** `X-Tenant-ID`

**Note:** Every update publishes `catalog.item.updated.v1` to Kafka.

---

## 11. Order Service

### Service: `order-service` — Port 8092

Central order orchestration with saga state management.

---

#### 11.1 Create Order

```
POST /api/v1/orders
Content-Type: application/json
```

**Required Headers:**
- `X-Tenant-ID`
- `Idempotency-Key` — unique per order attempt

**Request Body:**
```json
{
    "customerId": "customer-001",
    "storeId": "store-001",
    "channel": "ONLINE",
    "notes": "Please deliver before 5 PM",
    "lines": [
        {
            "sku": "LAPTOP-001",
            "itemName": "Business Laptop",
            "itemType": "PHYSICAL",
            "qty": 1,
            "unitPricePaise": 5000000,
            "taxPaise": 900000
        },
        {
            "sku": "MOUSE-001",
            "itemName": "Wireless Mouse",
            "itemType": "PHYSICAL",
            "qty": 2,
            "unitPricePaise": 150000,
            "taxPaise": 27000
        }
    ]
}
```

**Channels:** `ONLINE` | `POS` | `API`

**Item Types:** `PHYSICAL` | `SERVICE` | `DIGITAL` | `BUNDLE`

**Example Response:**
```json
{
    "success": true,
    "message": "Order created",
    "data": {
        "id": "uuid",
        "orderNumber": "ORD-1775579439868",
        "tenantId": "tenant-001",
        "status": "PENDING",
        "totalPaise": 5327000,
        "taxPaise": 954000,
        "channel": "ONLINE",
        "lines": [
            {
                "sku": "LAPTOP-001",
                "qty": 1,
                "totalPaise": 5900000
            },
            {
                "sku": "MOUSE-001",
                "qty": 2,
                "totalPaise": 327000
            }
        ]
    }
}
```

---

#### 11.2 Get Order by Order Number

```
GET /api/v1/orders/{orderNumber}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl "http://localhost:8092/api/v1/orders/ORD-1775579439868" \
  -H "X-Tenant-ID: tenant-001"
```

---

#### 11.3 List Orders by Status

```
GET /api/v1/orders?status={status}
```

**Required Headers:** `X-Tenant-ID`

**Status Values:** `PENDING` | `RESERVED` | `PAYMENT_PENDING` | `PAID` | `PROCESSING` | `DELIVERED` | `CANCELLED` | `FAILED` | `REFUNDED`

---

#### 11.4 Update Order Status

```
PATCH /api/v1/orders/{orderNumber}/status?status={status}
```

**Required Headers:** `X-Tenant-ID`

**Order Status Flow:**
```
PENDING
  └── RESERVED (inventory reserved)
        └── PAYMENT_PENDING
              └── PAID (triggers: order.paid.v1 Kafka event)
                    ├── PROCESSING
                    │     └── DELIVERED
                    └── FAILED
                          └── REFUNDED
```

**Example:**
```bash
curl -X PATCH "http://localhost:8092/api/v1/orders/ORD-1775579439868/status?status=PAID" \
  -H "X-Tenant-ID: tenant-001"
```

**When status = PAID, triggers:**
- `order.paid.v1` → ledger-service posts 3 entries
- `order.paid.v1` → inventory-service confirms reservation
- `notification.send.v1` → notification-service sends receipt

---

## 12. Seller Service

### Service: `seller-service` — Port 8093

Marketplace seller management with KYC, commission, and payouts.

---

#### 12.1 Register Seller

```
POST /api/v1/sellers?businessName={name}&email={email}&phone={phone}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8093/api/v1/sellers?businessName=TechShop&email=tech@shop.com&phone=9999999999" \
  -H "X-Tenant-ID: tenant-001"
```

**Response:**
```json
{
    "success": true,
    "message": "Seller registered",
    "data": {
        "sellerId": "SLR-A1B2C3D4",
        "businessName": "TechShop",
        "status": "PENDING_KYC"
    }
}
```

**Seller Status Flow:**
```
PENDING_KYC → ACTIVE → SUSPENDED → TERMINATED
```

---

#### 12.2 Activate Seller

```
POST /api/v1/sellers/{sellerId}/activate?commissionRate={rate}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8093/api/v1/sellers/SLR-A1B2C3D4/activate?commissionRate=8.5" \
  -H "X-Tenant-ID: tenant-001"
```

---

#### 12.3 Get Seller

```
GET /api/v1/sellers/{sellerId}
```

**Required Headers:** `X-Tenant-ID`

---

#### 12.4 List Active Sellers

```
GET /api/v1/sellers
```

**Required Headers:** `X-Tenant-ID`

---

#### 12.5 Simulate Sale (Commission Calculation)

```
POST /api/v1/sellers/{sellerId}/sales/simulate?orderId={orderId}&salePaise={amount}
```

**Required Headers:** `X-Tenant-ID`

**Example:**
```bash
curl -X POST "http://localhost:8093/api/v1/sellers/SLR-A1B2C3D4/sales/simulate?orderId=ORD-001&salePaise=5000000" \
  -H "X-Tenant-ID: tenant-001"
```

**Commission Calculation:**
```
Commission = salePaise × commission_rate / 100
TCS        = salePaise × 0.1 / 100
Net Payout = salePaise - Commission - TCS
```

---

#### 12.6 Get Seller Payouts

```
GET /api/v1/sellers/{sellerId}/payouts
```

**Required Headers:** `X-Tenant-ID`

**Payout Status:** `PENDING` → `INITIATED` → `SETTLED` | `FAILED`

---

## 13. End-to-End Flow

### Complete Purchase Flow

```
Step 1: Get JWT Token
  POST /api/v1/auth/token → access_token

Step 2: Calculate Tax
  POST /api/v1/tax/calculate → taxBreakdown

Step 3: Create Catalog Item (if not exists)
  POST /api/v1/catalog/items → itemId

Step 4: Add Inventory
  POST /api/v1/inventory/stores/{storeId}/skus/{sku}/stock

Step 5: Create Order
  POST /api/v1/orders → orderNumber

Step 6: Capture Payment
  POST /api/v1/payments/capture → tender

Step 7: Mark Order Paid
  PATCH /api/v1/orders/{orderNumber}/status?status=PAID
    └── Triggers: order.paid.v1 (Kafka)
          ├── ledger-service → posts ledger entries
          ├── inventory-service → confirms reservation
          └── notification-service → sends receipt email

Step 8: Verify Ledger
  GET /api/v1/ledger/orders/{orderId} → 3 ledger entries
```

---

### Complete Seller Sale Flow

```
Step 1: Register Seller
  POST /api/v1/sellers → sellerId (PENDING_KYC)

Step 2: Activate Seller with Commission Rate
  POST /api/v1/sellers/{sellerId}/activate?commissionRate=8.5

Step 3: Process Sale
  POST /api/v1/sellers/{sellerId}/sales/simulate
    └── Commission = 8.5% of sale
    └── TCS = 0.1% of sale
    └── Net payout = sale - commission - TCS

Step 4: Check Payouts
  GET /api/v1/sellers/{sellerId}/payouts
```

---

### Tenant Onboarding Flow

```
Step 1: Register Client
  POST /api/v1/admin/clients → tenantId (UNVERIFIED)

Step 2: Update to ACTIVE
  PATCH /api/v1/admin/clients/{tenantId}/status?status=ACTIVE

Step 3: Provision Tenant
  POST /api/v1/provisioning/tenants/{tenantId}?profile=FULL_COMMERCE
    └── Creates schema
    └── Runs migrations
    └── Seeds RBAC
    └── Issues credentials

Step 4: Check Provisioning Status
  GET /api/v1/provisioning/tenants/{tenantId}
```

---

## 14. Kafka Event Flows

### Published Events

| Event | Publisher | Consumers |
|---|---|---|
| `order.created.v1` | order-service | analytics |
| `order.paid.v1` | order-service | ledger, inventory, notification |
| `payment.captured.v1` | payment-adapter | ledger |
| `catalog.item.updated.v1` | catalog-service | opensearch-indexer |
| `notification.send.v1` | order-service | notification-service |
| `seller.sale.v1` | (external) | seller-service |
| `tenant.provisioned.v1` | provisioning | platform-admin |

### Event Payload Examples

#### `order.paid.v1`
```json
{
    "orderId": "uuid",
    "tenantId": "tenant-001",
    "orderNumber": "ORD-1234567890",
    "totalPaise": 5900000,
    "taxPaise": 900000
}
```

#### `payment.captured.v1`
```json
{
    "orderId": "order-001",
    "tenantId": "tenant-001",
    "amountPaise": 5900000,
    "gatewayPaymentId": "rzp_pay_001"
}
```

#### `notification.send.v1`
```json
{
    "tenantId": "tenant-001",
    "payload": {
        "recipient": "user@example.com",
        "channel": "EMAIL",
        "templateCode": "ORDER_PAID",
        "subject": "Order Confirmed",
        "body": "Your order has been confirmed."
    }
}
```

---

## 15. Common Headers

| Header | Required | Description |
|---|---|---|
| `X-Tenant-ID` | Yes (most endpoints) | Tenant identifier |
| `Authorization` | Yes (via gateway) | `Bearer {jwt_token}` |
| `Idempotency-Key` | Yes (write endpoints) | Unique request key |
| `Content-Type` | Yes (POST/PUT) | `application/json` |
| `X-Schema-Name` | Injected by gateway | DB schema name |
| `X-Tenant-Profile` | Injected by gateway | Tenant profile |

---

## 16. Error Responses

### Standard Error Format

```json
{
    "success": false,
    "error": {
        "code": "ERROR_CODE",
        "message": "Human readable message"
    },
    "timestamp": "2026-04-07T10:00:00Z"
}
```

### HTTP Status Codes

| Code | Meaning | When |
|---|---|---|
| 200 | OK | Successful GET/PATCH |
| 201 | Created | Successful POST |
| 202 | Accepted | Async operations |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid JWT |
| 403 | Forbidden | Tenant suspended or inactive |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate idempotency key |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Unexpected error |

### Common Error Codes

| Code | Description |
|---|---|
| `RESOURCE_NOT_FOUND` | Entity not found |
| `TENANT_NOT_FOUND` | Tenant does not exist |
| `IDEMPOTENCY_CONFLICT` | Request already processed |
| `INSUFFICIENT_INVENTORY` | Not enough stock |
| `INVALID_STATUS_TRANSITION` | Invalid order status change |
| `DOUBLE_ENTRY_FAILED` | Ledger debit/credit mismatch |

---

*Commerce OS — Smart AI-Powered Multi-Tenant Commerce Platform*
*Java 21 + Spring Boot | 13 Services | 5 Phases*
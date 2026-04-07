Smart AI-Powered Multi-Tenant Commerce OS — Dev Reference

Java 21 + Spring Boot | 13 Services | 5 Phases | 23 Weeks

1. Repo Structure

commerce-os/
├── platform/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── platform-admin-service/   # absorbs: outbox, quota, tenant-config, platform-billing
│   ├── provisioning-service/
│   └── notification-service/
├── commerce/
│   ├── tax-calculation-service/
│   ├── billing-service/          # absorbs: POS, wallet, subscriptions
│   ├── payment-adapter-service/
│   ├── ledger-service/
│   ├── inventory-service/        # absorbs: webhook-dispatcher
│   ├── catalog-service/          # absorbs: opensearch-indexer, search, slot-booking
│   └── order-service/            # absorbs: delivery, AI sidecar
├── marketplace/
│   └── seller-service/           # absorbs: commission, payout, compliance, KYC
├── shared/
│   ├── common-models/
│   ├── kafka-contracts/
│   ├── avro-schemas/
│   ├── tenant-context/
│   ├── resilience-config/
│   └── test-utils/
├── infra/
│   ├── k8s/
│   │   ├── base/
│   │   ├── overlays/
│   │   │   ├── billing-only/
│   │   │   ├── inventory-only/
│   │   │   ├── pos-only/
│   │   │   ├── web-commerce/
│   │   │   └── full-commerce/
│   │   └── keda/
│   ├── helm/
│   ├── terraform/
│   └── docker-compose/
├── db/
│   ├── platform/flyway/
│   └── tenant/
│       ├── profiles/
│       │   ├── billing-only/
│       │   ├── inventory-only/
│       │   ├── pos-only/
│       │   ├── pos-billing/
│       │   ├── web-commerce/
│       │   └── full-commerce/
│       └── modules/
│           ├── service-management/
│           └── slot-booking/
└── docs/
2. Internal Layout (All Services)

Merged concerns are packages inside the service, not separate pods.

{service}/src/main/java/com/commerceos/{service}/
├── api/
│   ├── rest/          # one controller per module
│   └── kafka/         # Kafka listeners
├── application/
│   ├── commands/
│   ├── queries/
│   └── handlers/
├── domain/
│   ├── model/
│   ├── enums/
│   └── events/
├── infrastructure/
│   ├── persistence/
│   ├── kafka/
│   ├── redis/
│   ├── temporal/      # workflows + activities
│   ├── external/      # gateway clients, GST/NSDL APIs
│   └── scheduler/     # background jobs: outbox poll, slot gen, retries
└── config/

{service}/sidecar/     # Python AI agents — order, billing, inventory only
├── agents/
├── fallbacks/
└── main.py
3. Build Order

Phase 0 — Weeks 1–2   (Platform foundation)
shared/* → auth-service → api-gateway → platform-admin → provisioning → notification

Phase 1 — Weeks 3–6   (Core commerce)
tax → payment-adapter → billing → inventory → catalog → ledger → order

Phase 2 — Weeks 7–10  (Operations — no new services)
Wire: service.completed.v1 → ledger, delivery.completed.v1 → ledger
GRN flow, refund policies, B2B overdue escalation, all reconciliation tiers live

Phase 3 — Weeks 11–14 (Intelligence — no new services)
Activate AI sidecars (billing + inventory), KEDA ScaledObjects, tier promotion job

Phase 4 — Weeks 15–19 (Marketplace)
seller-service, extend order-service for ChildSellerOrderSaga

Phase 5 — Weeks 20–23 (Scale)
analytics-service (ClickHouse boundary), multi-currency, data archival, credential rotation
4. Services & What Each Must Build

#	Service	Domain	Profile
1	api-gateway	Platform	All
2	auth-service	Platform	All
3	platform-admin-service	Platform	All
4	provisioning-service	Platform	All
5	notification-service	Platform	All
6	tax-calculation-service	Commerce	Billing+
7	billing-service	Commerce	Billing+
8	payment-adapter-service	Commerce	Billing+
9	ledger-service	Commerce	Billing+
10	inventory-service	Commerce	Inventory+
11	catalog-service	Commerce	Web Commerce+
12	order-service	Commerce	Web Commerce+
13	seller-service	Marketplace	Full Commerce
1. api-gateway

JWT validation against JWKS — stateless, no DB call
Tenant resolution: tenant_id from JWT → tenant_identity (Redis 1h TTL → DB fallback) → inject X-Tenant-ID + X-Schema-Name headers
Module gate: client_module_activations (Redis → DB) → 403 if inactive
Rate limiting per tenant (Redis token bucket)
IP whitelist from store_settings; route table from profile_service_map (Redis-cached)
mTLS on all upstream forwarding
2. auth-service

OAuth2 Authorization Server — client credentials grant (machine-to-machine)
JWT: claims tenant_id, user_id, roles, exp; RSA-signed
JWKS endpoint /.well-known/jwks.json; token revocation endpoint
api_credentials: client_id + bcrypt-hashed client_secret
Credential rotation (Phase 5): 90-day cycle, 7-day overlap window, 14-day advance notification
3. platform-admin-service

Client lifecycle: registration → GSTIN (GST portal) + PAN (NSDL) + Email OTP → plan + profile → status machine UNVERIFIED → VERIFIED → PAYMENT_DONE → ACTIVE → SUSPENDED; every write to admin_audit_log
Outbox polling: each service polls its own outbox_events rows via SELECT … FOR UPDATE SKIP LOCKED; platform-admin polls only its own rows; 5× retry with backoff; DLQ alert
Quota metering: Redis INCR quota:{tenant_id}:{metric}:{window} per request; background flusher batches to quota_counters DB every 30–60s; nightly rollup to usage_metrics; quota check reads Redis (DB fallback on miss)
Tenant config: store_settings CRUD (global → store → override); feature_flags; tax_mode change guard (block if open Redis reservations); cache-invalidation event on every write
Platform billing: subscription invoices per client; retry immediate → +1h → +24h → suspend; grace period before suspension
Tier promotion: nightly — Standard → Professional triggers UpgradeWorkflow; Enterprise always manual
4. provisioning-service

ProvisioningWorkflow on Platform Temporal — every step has a compensation:
CreateSchemaActivity — CREATE SCHEMA tenant_{slug}
RunMigrationsActivity — Flyway runs db/tenant/profiles/{profile}/
CreateMongoNamespaceActivity
CreateOpenSearchIndexActivity — web-commerce / full-commerce only
DeployK8sServicesActivity — apply infra/k8s/overlays/{profile}/
SeedRbacActivity — default roles + feature flags
IssueCredentialsActivity — write api_credentials, set client.status = ACTIVE
Enterprise: pause at step 5 for admin approval signal
UpgradeWorkflow — additive delta migrations only
SuspensionWorkflow — set client.status = SUSPENDED + invalidate Redis cache; gateway returns 403
Every step writes progress to provisioning_jobs
5. notification-service

Kafka consumer for notification.send.v1
Channels: email (SES), SMS (SNS/Twilio), push
Recipient resolved from client_contacts by type; Thymeleaf templates
Retry 3× exponential backoff; DLQ alert
6. tax-calculation-service

Single stateless REST endpoint — no DB writes, no state
Input: { base_price_paise, tax_mode, tax_code_type (HSN/SAC), tax_code, qty, uom }
Output: { tax_breakdown[], total_paise, display_mode }
TAX_INCLUSIVE: base = round(gross / (1 + rate/100), HALF_UP); TAX_EXCLUSIVE: tax added on top
HSN + SAC slabs from read-only table; refresh every 24h
All arithmetic BigDecimal; result as long paise — never float
7. billing-service

Payment session: up to 10 tenders, integer paise; tender sum == order total exactly; parallel capture; any failure → compensate all; coupon atomic CAS; duplicate guard via partial unique index on (order_id) WHERE status = COMPLETE
Invoice: invoices + invoice_line_items; Thymeleaf (AI sidecar from Phase 3); B2B credit enforced before acceptance; net-30/60 terms; overdue escalation at 7 / 14 / 30 days
POS channel: tax mode frozen at session open; PHYSICAL → pessimistic lock deduct; SERVICE INSTANT → delivered at payment; SERVICE SCHEDULED → confirmed slot_booking; end-of-day reconciliation
Wallet: SELECT FOR UPDATE + CHECK (balance >= 0); wallet_transactions append-only
Subscriptions: recurring billing at cycle end; PHYSICAL → dispatch; SERVICE SCHEDULED → next slot; SERVICE RECURRING → entitlement reset; retry immediate → +1h → +24h → SUSPENDED; proration = (remaining_days / cycle_days) × plan_price; tax_mode + base_price_paise frozen at creation
8. payment-adapter-service

Unified interface over Razorpay, Stripe, PhonePe — each a separate adapter implementation
Operations: capture, refund, status_poll, webhook_ingest; idempotency key on every call
Status: PENDING → CAPTURED / FAILED; on CAPTURED publish payment.captured.v1
5-min reconciliation job: gateway API vs payment_tenders → alert on mismatch
9. ledger-service

ledger_entries append-only, RANGE-partitioned by month on created_at; DB trigger enforces zero UPDATE / DELETE
Non-seller 4-entry: DEBIT Receivable / CREDIT GST Collected / CREDIT Revenue / CREDIT GST Payable
Seller 7-entry: + Commission Revenue / Platform GST Payable (18%) / TCS Payable (0.1%) / Seller Net Payable; assert sum(debits) == sum(credits) on every write
Separate gst_ledger + tcs_ledger append-only partitioned tables
Reconciliation: Tier 1 — same DB tx as payment; Tier 2 — 5-min gateway vs tenders; Tier 3 — nightly batch
Alert if any amount stuck > 5 min
10. inventory-service

store_inventory: available_qty, reserved_qty, damaged_qty, expired_qty
inventory_movements append-only, RANGE-partitioned by month — never direct qty update
Online reservation: Redis atomic DECR → pending_reservations synchronously → background DB deduct; auto-release on expiry; Redis crash → rebuild from pending_reservations
POS reservation: SELECT FOR UPDATE
inventory_batches for lot + expiry; FEFO picking
PO → GRN: receive, validate qty, add to stock; handle short / over / damaged
Inter-store transfer: REQUESTED → APPROVED → IN_TRANSIT → RECEIVED; both stores locked during transit
Webhook dispatcher (inventory-only profile): HMAC-SHA256 payloads to ERP; exponential retry → DLQ; background thread
11. catalog-service

Catalog: one MongoDB DB per profile (catalog_web_commerce, catalog_full_commerce); all tenants share collection; every document has tenant_id; compound index { tenant_id, status, item_type }; shard key { tenant_id: "hashed" }; CRUD for PHYSICAL, SERVICE, DIGITAL, BUNDLE; prices always base_price_paise; on every write publish catalog.item.updated.v1 via outbox
OpenSearch indexer: Kafka consumer for catalog.item.updated.v1; idempotent upsert; 3-retry DLQ; nightly MongoDB vs OpenSearch count reconciliation
Search: tenant-scoped endpoints from OpenSearch only — never MongoDB; full-text + faceted filters
Slot booking: nightly generation for next 30 days per SCHEDULED item; SlotRegenerationWorkflow on Platform Temporal on config change; Redis slot counter + pending_slot_reservations; booking RESERVED → CONFIRMED → COMPLETED / CANCELLED / NO_SHOW; on COMPLETED publish service.completed.v1
12. order-service

Idempotency: idempotency_keys (Redis → DB) on every order create
ParentOrderSaga (Tenant Temporal):
Bundle explosion → catalog-service expands BUNDLE lines
Reserve by type: PHYSICAL → inventory Redis; SERVICE SCHEDULED → catalog slot Redis; others skip
Any failure → compensate all reserves → fail order, no payment taken
Payment → billing-service multi-tender session
Post-payment: PHYSICAL → delivery; SERVICE SCHEDULED → confirm slot; SERVICE INSTANT → DELIVERED; MILESTONE → create milestone rows
Write ledger → invoice → publish order.paid.v1
saga_state for crash resume; saga_fallback_log for all compensations
Delivery: on order.paid.v1 for PHYSICAL; AI sidecar driver assignment (fallback: distance ASC); tracking ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED / FAILED; publish delivery.completed.v1
Multi-seller (Phase 4): one ChildSellerOrderSaga per seller; all confirm before parent captures; child failure → partial refund for that seller only
13. seller-service

KYC: SellerKycWorkflow on Platform Temporal — PAN → GSTIN → bank penny-drop → admin approval; status PENDING_KYC → ACTIVE → SUSPENDED → TERMINATED; seller JWT adds seller_id + seller_role
Listings: admin approval snapshots commission rate to seller_commission_rates — immutable forever
Auto-suspension: nightly 30-day rolling — unfulfilled > 5%, complaints > 3%, counterfeit > 2, rating < 2.5 → immediate, non-reversible without admin
Commission + payout: consume seller.sale.v1; commission = base_price_paise × snapshot_rate / 100; 7-entry ledger set; payout PENDING → INITIATED → SETTLED / FAILED; 3 failures → flag for bank re-verification; refunds post-payout → seller_debit_notes
Compliance: TCS 0.1% → seller_tcs_ledger; Form 26Q monthly; GSTR-8 export; TCS certificate download
5. Non-Negotiable Rules

Security

Every service validates JWT independently — no trust delegation
tenant_id from JWT claim only — never from request body; missing → reject before any DB op
All inter-service calls mTLS
SET LOCAL search_path only in TenantTransactionSynchronizationAdapter; PgBouncer in transaction-pooling mode
Idempotency

Every write endpoint requires Idempotency-Key header; checked against idempotency_keys (Redis → DB, 24h TTL)
Every Kafka consumer checks processed_kafka_events ({topic}:{partition}:{offset}) before processing
Data

All amounts: long paise — never double or float
All prices in DB: base_price_paise (pre-tax) — display derived at read time, never stored
ledger_entries: DB trigger enforces zero UPDATE or DELETE; tenant_id explicit everywhere — never ThreadLocal in async
ledger_entries, inventory_movements, audit_logs, processed_kafka_events: RANGE-partitioned on created_at by month from day one
Scalability (Breaks at 500+ Tenants if Ignored)

Quota counters: Redis INCR per request + batched DB flush every 30–60s — not Kafka→DB per request
Suspension: status flag + gateway 403 only — never scale shared K8s deployments to 0
Outbox polling: each service polls its own rows with SELECT … FOR UPDATE SKIP LOCKED — never a single centralised poller
MongoDB: one DB per profile + tenant_id field + sharded collections — never one namespace/database per tenant
Append-only tables: RANGE-partition from day one — never leave them unpartitioned
Observability

Every service emits: { service, tenant_id, trace_id, operation, duration_ms, status }
Trace ID via OpenTelemetry in every HTTP header + Kafka message header
Grafana alerts: DLQ > 0, circuit breaker OPEN, p99 breach, Redis down > 30s, Temporal stuck > 10 min, reconciliation mismatch, wallet balance < 0
AI Sidecars

Circuit breaker: 50% failure over 10 calls → open; resets after 30s
Rule-based fallback must exist and be tested before any agent is turned on
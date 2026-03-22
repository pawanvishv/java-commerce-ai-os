# java-commerce-ai-os
A next-generation AI-powered multi-tenant Commerce OS that unifies inventory, orders, payments, analytics, and automation across multiple business channels using a scalable microservices architecture.

# Smart AI-Powered Multi-Tenant Commerce OS

Project Reference Guide — v9.0 Optimised


1. What This System Is

A multi-tenant SaaS commerce platform. Each client (tenant) operates their own isolated store environment. The platform supports six deployment profiles — from a simple billing-only tool up to a full marketplace with AI routing, seller commissions, and multi-warehouse inventory.

Every client picks a profile at registration. The platform provisions exactly the services and database tables that profile requires and nothing more.


2. Tech Stack

Layer	Technology
Backend	Java 21 + Spring Boot
API Gateway	Spring Cloud Gateway
Auth	OAuth2 + JWT (Spring Security)
Events	Kafka + Avro Schema Registry
Cache	Redis Cluster (DB fallback on every key)
Core DB	PostgreSQL shared cluster, schema-per-tenant
Catalog + Logs	MongoDB shared cluster, collection-per-tenant
Search	OpenSearch, index-per-tenant
Saga Orchestrator	Temporal.io (two clusters — platform + tenant)
AI	Python + LangChain
Resilience	Resilience4j (circuit breaker, bulkhead, retry)
Observability	Prometheus + Grafana + OpenTelemetry
Infra	Kubernetes + KEDA + Docker

3. Six Deployment Profiles

A client selects one profile at onboarding. Each profile is a pre-defined bundle of services and database tables. Upgrades are always additive — no data is dropped.

Profile	Services deployed	DB tables	Kafka	Temporal
Billing only	billing, tax, payment adapter, notifications	~12	No	No
Inventory only	inventory, notifications, webhook dispatcher	~18	Optional	No
POS only	POS billing (receipt mode), inventory (deduct only), tax	~20	No	No
POS + Billing	POS, full billing, tax, inventory (deduct + adjust)	~28	No	No
Web commerce	All above + order, catalog, delivery, OpenSearch	~52	Yes	Yes (tenant)
Full commerce	Everything + AI agents, seller marketplace	~75	Yes	Yes (tenant)
The SERVICE_MANAGEMENT and SLOT_BOOKING modules are optional add-ons available on any profile that includes billing.


4. Client Registration and Provisioning

Flow

1. Client submits business details
       → Writes: platform_admin.clients, client_contacts

2. Verification
       → GSTIN validated (GST portal API)
       → PAN validated (NSDL API)
       → Email OTP
       → client.status = VERIFIED

3. Plan + profile selection
       → Writes: client_plans (links to deployment_profiles)

4. Platform subscription payment
       → Writes: platform_invoices
       → client.status = PAYMENT_DONE

5. Provisioning saga (Platform Temporal — always on)
       → CREATE SCHEMA tenant_{slug} in PostgreSQL
       → Run profile-scoped Flyway migrations
       → Create MongoDB namespace
       → Create OpenSearch index (if profile needs it)
       → Deploy K8s services from profile_service_map
       → Seed RBAC roles + feature flags
       → Issue OAuth2 credentials
       → Each step has a compensation — any failure triggers rollback

6. Enterprise clients: manual admin review gate
   Standard/Professional: auto-activated

7. client.status = ACTIVE
       → OAuth2 credentials delivered to technical contact
       → Tenant schema live
Every step is tracked in provisioning_jobs with a step-by-step log. The admin dashboard polls this during provisioning and shows a live progress bar.


Platform Admin Database (platform_admin schema)

This schema is completely isolated from all tenant schemas. Only five platform-level services touch it.

Table	Purpose	Used by
clients	Master client record, status, tier	platform-admin, api-gateway, billing
client_contacts	Primary / billing / technical contacts	notification-service
client_plans	Plan, profile, fee, quotas, billing cycle	provisioning, api-gateway
deployment_profiles	Profile definitions with service + table manifests	provisioning
profile_service_map	Service + feature_gates per profile	provisioning, tenant services at startup
client_module_activations	Fine-grained module toggles per client	api-gateway (every request, Redis-cached)
tenant_identity	JWT tenant_id → schema_name + redis_keyspace	api-gateway (every request, Redis-cached)
tenant_schemas	Infra record — cluster, pool, namespaces, migration version	provisioning
service_instances	Running K8s deployments + health status	ops monitoring
provisioning_jobs	Temporal workflow tracking, step-by-step log	admin dashboard
api_credentials	OAuth2 client_id + bcrypt-hashed secret	auth-service
quota_counters	DB-primary billable event counters (API calls, invoices)	api-gateway, metering
usage_metrics	Daily rollup from quota_counters	platform-billing
platform_invoices	Platform's own invoice to each client	platform-billing
outbox_events	Transactional outbox for module activation sync	outbox-processor
admin_audit_log	Immutable log of every admin action (sensitive fields redacted)	compliance

5. Multi-Tenancy — How Isolation Works

One shared PostgreSQL cluster. One schema per tenant (CREATE SCHEMA tenant_acme). Every transaction opens with SET LOCAL search_path = tenant_{slug} — this is enforced by a Spring TransactionSynchronizationAdapter and resets automatically when the transaction ends. PgBouncer runs in transaction-pooling mode, so the search_path is always per-transaction, never per-connection.

The tenant_identity table in platform_admin maps every JWT tenant_id claim to the correct schema name. The API gateway resolves this on every request (Redis-cached, 1-hour TTL) and sets the search path before forwarding.

Tenant ID is passed as an explicit parameter through every service method, Kafka message header, and Temporal workflow input. It is never stored in ThreadLocal for async operations.

Read-only queries (catalog lookups, order history, reporting) route to read replicas automatically via @Transactional(readOnly=true) and a routing datasource.

Large tenants are tiered: Standard (shared cluster), Professional (dedicated node + PgBouncer pool), Enterprise (dedicated cluster, Redis, Kafka partitions, Temporal namespace).


6. Catalog — Unified Item Model

All items — physical products and services — live in the same MongoDB catalog collection. The item_type field drives all downstream behaviour.

Catalog → OpenSearch sync: Every catalog item create, update, or status change publishes a catalog.item.updated.v1 Kafka event (keyed by {tenant_id}:{item_id} for ordering). A dedicated opensearch-indexer consumer group processes these events, upserts the document into the tenant's OpenSearch index, and commits the offset only after the upsert succeeds. The consumer is idempotent — processing the same event twice produces the same index state. Failed upserts retry three times with exponential backoff then route to the DLQ, which triggers an alert. A nightly full-reconciliation job sweeps MongoDB and OpenSearch counts per tenant and flags any divergence for re-indexing. OpenSearch is used exclusively for search and discovery — catalog-level decisions (seller suspension, price validation, tax code lookup) always read from MongoDB as the source of truth.


Item Types

item_type	Inventory	Slot booking	Delivery	Tax code	Revenue recognition
PHYSICAL	Redis reserve → DB deduct	No	Yes (delivery-service)	HSN	On delivery
SERVICE	Skip entirely	If SCHEDULED	Skip	SAC	On completion / milestone
DIGITAL	Skip	No	Skip (auto-deliver)	SAC/HSN	On payment
BUNDLE	Per component type	Per component	Per component	Per component	On fulfilment
Bundle expansion: At the start of the order saga, before any reservation step, the saga calls the catalog service to explode each BUNDLE line into its component child lines. Each child line is inserted into order_lines with its own item_type, catalog_item_id, quantity, and base_price_paise. The parent bundle line is retained for display purposes only (invoice shows the bundle name; the child lines drive all financial calculations). From this point, each child line is treated as a fully independent item — PHYSICAL children go through inventory reservation, SERVICE children go through slot reservation or direct completion, and so on. For multi-seller bundles where components come from different sellers, each component creates its own order_seller_split entry and its own commission and TCS ledger entries. The parent bundle's base_price_paise is the sum of all child base prices — discounts applied to the bundle are distributed proportionally across children before commission calculation. Full bundle support (multi-seller component explosion, partial fulfilment of bundle components) is scoped to Phase 3–4.


Key Catalog Fields

All items share: item_type, name, base_price_paise (always pre-tax), tax_code_type (HSN or SAC), tax_code, uom(EA / KG / HR / SESSION / VISIT), tax_rate_pct, status.

Physical-only: track_inventory, reorder_point, weight_grams.

Service-only: service_type (INSTANT / SCHEDULED / MILESTONE / RECURRING), duration_minutes, max_capacity, provider_required, is_refundable, refund_cutoff_hours.


Service Sub-Types

INSTANT — no slot, delivered at payment (consultation fee, processing charge). SCHEDULED — slot reserved at checkout (haircut, doctor, home repair). MILESTONE — phased delivery tracked in service_milestones table (project, legal case). RECURRING — subscription-linked with entitlement session tracking (gym, retainer, AMC).


7. Tax Mode — Store-Level Setting

Every store declares tax_mode: TAX_INCLUSIVE (price already includes tax — Indian retail MRP) or TAX_EXCLUSIVE (tax added at checkout — B2B, EU VAT, US sales tax).

All prices stored in DB are always base_price_paise (pre-tax). For TAX_INCLUSIVE stores, the system extracts the base on save using base = round(gross / (1 + rate/100), HALF_UP). Display price is derived at read time and never stored.

The tax calculation service always receives base_price_paise, tax_mode, tax_code_type, and tax_code per line. It never infers mode from context. Every order, subscription, and POS session stamps tax_mode at creation — changing the store setting never corrupts historic records.

Coupons and discounts always apply to base_price_paise. Seller commissions always apply to base_price_paise. The tax mode only changes how totals are presented to the customer and formatted on invoices.

Tax mode change is blocked if there are open Redis reservations (active carts). Every change writes an audit log entry.


8. Order Flow

Customer places order
    │
    ▼
Idempotency check → duplicate? Return cached response
    │
    ▼
ParentOrderSaga starts (Tenant Temporal)
    │
    ▼
AI Routing → store selected (circuit breaker → rule-based fallback)
    │
    ▼
Per line item — check item_type:
  PHYSICAL      → Redis inventory reserve (pending_reservations as fallback)
  SERVICE SCHED → Redis slot reserve (pending_slot_reservations as fallback)
  SERVICE OTHER → no reservation
    │
    ├── Any reservation fails → compensate all, fail order, no payment
    │
    ▼
Payment captured once at parent level (multi-tender session)
    │
    ▼
Per line item — post-payment:
  PHYSICAL      → delivery-service assigns driver + ETA
  SERVICE SCHED → slot_booking confirmed, staff notified
  SERVICE INST  → marked DELIVERED immediately
  SERVICE MILE  → service_milestones created
    │
    ▼
Ledger written → Invoice generated → Kafka published

Multi-Tender Payment

Up to 10 tenders per session (cash, credit card ×3, debit card, UPI, wallet, coupon). All amounts in integer paise — no float arithmetic. Tender sum must equal order total exactly. All tenders processed in parallel. If any fail: compensation chain runs, all captured tenders refunded. Ledger written only after all tenders are CAPTURED.

Coupon: atomic compare-and-swap DB update (fails if already redeemed). Wallet: SELECT FOR UPDATE + DB CHECK constraint (balance ≥ 0). Session duplicate: partial unique index on (order_id) WHERE status = COMPLETE.


9. Inventory (Physical Items Only)

Lifecycle: Purchase Order → GRN → Stock Add → Sale → Adjustment → Audit

Stock is stored in store_inventory (available_qty, reserved_qty, damaged_qty, expired_qty). Every change writes an append-only inventory_movements record — never a direct update.

High-traffic reservation (online): Redis atomic decrement gates the purchase. A pending_reservations DB row is written synchronously in the same request as proof. On Redis crash, recovery rebuilds counters from pending_reservations. Background worker converts reservations to DB deductions. Expired reservations release automatically.

POS reservation: Pessimistic lock (SELECT FOR UPDATE) — acceptable for low-volume counter traffic.

Batch/lot/expiry tracking: inventory_batches table. Picking uses FEFO (first expiry, first out).

Inter-store transfers: Full lifecycle (requested → approved → in-transit → received). Both stores locked during transit.

Inventory-only profile (no orders): Stock issues are raised manually against a reference document (production job, department request, ERP order). A webhook_dispatch_queue notifies the client's ERP system with HMAC-signed payloads and exponential retry.


10. Slot Management (Service Items — SCHEDULED Type)

Slots are pre-generated nightly for the next 30 days based on service operating hours, duration, and capacity.

Slot reconfiguration on config change: When a service's operating hours, duration, max_capacity, or provider assignment changes, a SlotRegenerationWorkflow runs immediately (triggered by the catalog update event, executed in Platform Temporal). The workflow: locks all future unbooked slots for that service, deletes them, regenerates new slots from the updated config, and releases the lock. Slots that already have confirmed bookings are never deleted — they remain under the old config. Affected customers receive a notification only if their booked slot falls outside the new operating hours, in which case the booking is flagged as REQUIRES_RESCHEDULE and the customer is offered alternative slots or a full refund. This keeps existing reservations safe while future capacity reflects the updated configuration.

service_slots: catalog_item_id, store_id, provider_id, slot_date, slot_start_time, slot_end_time, max_capacity, booked_count, status.

slot_bookings: slot_id, order_id, customer_id, qty, status (RESERVED → CONFIRMED → COMPLETED / CANCELLED / NO_SHOW).

Slot reservation uses the same Redis counter pattern as inventory. pending_slot_reservations provides durability on Redis crash.

When the service is completed, staff marks the booking COMPLETED. This fires service.completed.v1 Kafka event → revenue recognition ledger entry → customer rating prompt.


11. Billing System

Billing Entry Channels

POS terminal, online order, subscription auto-billing, B2B invoice (net-30/60 credit).


Tax Calculation

Delegated entirely to tax-calculation-service. Receives { base_price_paise, tax_mode, tax_code_type, tax_code, qty, uom } per line. Returns { tax_breakdown, total_paise, display_mode }. For physical items: HSN code, applicable GST slab. For service items: SAC code, applicable service tax slab.


Double-Entry Ledger

Immutable, append-only. Every transaction writes a balanced debit-credit set. For seller sales, the 7-entry set is:

DEBIT Customer Receivable (full payment incl. GST)
CREDIT Seller GST Collected (pass-through, seller's liability)
CREDIT Platform Commission Revenue (on pre-tax base)
CREDIT Platform GST Payable (18% on commission)
CREDIT TCS Payable (0.1% of gross — Section 194-O, Finance Act 2024)
CREDIT Seller Net Payable
Verification: debits = credits ✓
For non-seller sales: 4-entry set (receivable, tax, revenue, GST payable).


Reconciliation

Three tiers: per-transaction sync (ledger written in same DB transaction as payment capture), 5-minute near-real-time check (gateway API vs payment_tenders table), nightly batch (full settlement report). Money is never stuck for more than 5 minutes without an alert.


B2B Credit

Credit limit, net-30/net-60 terms. Credit drawn on total_paise (gross including GST). Limit enforced before order acceptance. Overdue invoices tracked with auto-escalation.


12. Seller Marketplace

Registration

Sellers are third parties who list products or services on the platform. KYC via Temporal workflow: PAN verification (NSDL), GSTIN validation, bank account penny-drop. Status: PENDING_KYC → ACTIVE → SUSPENDED → TERMINATED.


Fee Model

Annual platform fee (subscription, grace period on failure, listings hidden post-grace but in-flight orders complete). Per-sale commission: snapshotted at listing approval time — immutable regardless of future rate changes. Commission is always on base_price_paise (pre-tax).


Order Flow — Multi-Seller Cart

ParentOrderSaga spawns one ChildSellerOrderSaga per seller sub-order. All children confirm reservations before payment is captured at the parent level once. Post-payment child failure → partial refund for that seller only, other children proceed.


Payouts

Three states: PENDING → INITIATED → SETTLED or FAILED. Failed payouts re-queue automatically in seller_payable ledger. Three consecutive failures flag the account for bank detail re-verification. Post-payout customer refunds create seller_debit_notes (deducted from next payout) — original ledger entries are never reversed.


Compliance

TCS 0.1% deducted per sale. Monthly Form 26Q generation. Seller TCS certificate download. GSTR-8 data for platform. Seller's product GST is pass-through — seller remits via their own GSTR-3B.


Auto-Suspension Thresholds

Unfulfilled order rate > 5%, complaint rate > 3%, counterfeit reports > 2, or rating < 2.5 stars (all 30-day rolling). Immediate suspension, non-reversible without admin review.


13. Subscription System

Subscriptions reference catalog_item_id — works for both physical and service items.

Physical subscriptions: recurring billing → inventory pick and dispatch. Service subscriptions (SCHEDULED): recurring billing → next slot_booking created. Service subscriptions (RECURRING): recurring billing → service_entitlementsrenewed. Entitlement tracks sessions_allowed vs sessions_used. Exhausted entitlement blocks access until renewal.

Payment retry: immediate → +1 hour → +24 hours → SUSPENDED with notification. Proration: (remaining_days / cycle_days) × new_plan_price. Tax mode and base_price_paise frozen at subscription creation time.


14. POS Billing

POS session opens with tax_mode resolved from store settings hierarchy. Item list shows both PHYSICAL and SERVICE items with their tax codes.

PHYSICAL line: stock deducted (pessimistic lock), HSN on receipt. SERVICE INSTANT line: marked delivered at payment, SAC on receipt. SERVICE SCHEDULED line: slot_booking created with CONFIRMED status, receipt shows appointment details.

End-of-day: terminal drawer vs session totals reconciliation. Discrepancies flagged for manager review.


15. AI Agents (All with Circuit Breaker Fallback)

Agent	Primary	Fallback
Routing	Nearest store, lowest SLA	Distance ASC + priority DESC
Inventory	Stockout prediction, reorder trigger	Fixed reorder-point threshold
Pricing	Dynamic surge pricing	Base price from catalog
Recommendation	Personalised upsell	Top-sellers by category
Invoice	Smart invoice generation	Thymeleaf template
Fraud	Transaction anomaly detection	Velocity + amount threshold
Circuit breaker: opens at 50% failure rate over 10 calls, resets after 30 seconds. No order flow is ever blocked by AI unavailability.


16. Infrastructure Rules

Kafka

All order-lifecycle topics keyed by {tenant_id}:{order_id} — guarantees per-order event ordering. Non-order topics (audit, analytics) keyed by tenant_id. Every topic has a paired DLQ. After 3 retries with exponential backoff, failed messages land in DLQ and trigger an alert.

Every Kafka consumer checks processed_kafka_events ({topic}:{partition}:{offset}) before processing. This table is range-partitioned by date — old partitions dropped instantly. Naturally idempotent consumers (audit writes) skip the check.


Redis

Speed layer — never a hard dependency. Every key has a defined DB fallback:

Key pattern	Fallback
Inventory reservation counter	PostgreSQL SELECT FOR UPDATE
Slot reservation counter	PostgreSQL SELECT FOR UPDATE
Idempotency dedup	DB idempotency_keys table
Rate limit counter	In-memory per-pod limiter
Distributed lock	DB optimistic lock (version column)
Session / auth	Stateless JWT
Feature flags	DB feature_flags table
Tenant identity	DB tenant_identity table
Redis health check every 10 seconds. On failure: automatic degraded mode. On recovery: caches rebuilt from DB.


Temporal — Two Clusters

Platform Temporal (always on): runs ProvisioningWorkflow, UpgradeWorkflow, SuspensionWorkflow. Used by billing-only, inventory-only, and all other profiles. Tenant Temporal (provisioned only for web/full commerce): runs OrderSaga, ParentOrderSaga, SellerOrderSaga.


Kubernetes / KEDA

All consumer deployments: minReplicaCount: 2. High-traffic consumers (order, payment, inventory events): minReplicaCount: 4. Pre-scale cron triggers fire 10 minutes before scheduled promotional events. KEDA scales on Kafka consumer group lag.


Observability

Structured JSON logs: service, tenant_id, trace_id, operation, duration_ms. Trace ID flows through every Kafka header and service call via OpenTelemetry. Grafana alerts on: DLQ spike, circuit breaker open, p99 latency breach, Redis unavailability, Temporal workflow stuck >10 min, reconciliation mismatch, wallet balance negative.


17. Security

JWT contains tenant_id, user_id, roles. JWT tenant_id determines the schema search path — missing or invalid claim rejects the connection before any query runs. All inter-service calls use mTLS. Rate limiting per-tenant at the gateway. IP whitelisting configurable per tenant. Every service validates JWT independently (zero-trust).

Seller JWT additionally contains seller_id and seller_role. Seller users can never access platform operator data or other sellers' data.


18. Database Tables Reference

platform_admin schema (control plane — never accessed by tenant services)

clients, client_contacts, client_plans, deployment_profiles, profile_service_map, client_module_activations, tenant_identity, tenant_schemas, service_instances, provisioning_jobs, api_credentials, quota_counters, usage_metrics, platform_invoices, outbox_events, admin_users, admin_audit_log.


Tenant schema (per client — tables provisioned per profile)

Always provisioned: store_settings, feature_flags, rbac roles, audit_logs (MongoDB)

Billing profile and above: invoices, invoice_line_items, payment_sessions, payment_tenders, ledger_entries, customers, customer_addresses, idempotency_keys, processed_kafka_events, tcs_ledger, gst_ledger, tender_refund_policies

Inventory profile and above: stores, store_inventory, inventory_movements, inventory_batches, inventory_adjustments, inventory_audit, purchase_orders, goods_receipt, pending_reservations, inventory_forecasts, webhook_endpoints, webhook_dispatch_queue

Slot management module: service_slots, slot_bookings, service_refund_policies, service_entitlements, service_milestones

Order / web commerce and above: orders, order_lines, order_splits, saga_state, saga_fallback_log, order_waitlist, pending_slot_reservations, subscriptions, wallet, wallet_transactions

Seller marketplace: sellers, seller_products, seller_commission_rates, order_seller_splits, seller_pending_payouts, seller_payouts, seller_debit_notes, commission_adjustments, seller_payout_failures, seller_subscriptions, seller_tcs_ledger, seller_gst_ledger, parent_order_sagas, child_seller_sagas

Full commerce additions: delivery_assignments, delivery_tracking, inventory_demand_forecasts, pos_sessions, pos_terminals, pos_items, store_transfers


19. Implementation Phases

Phase 0 — Foundation (Weeks 1–2)

Platform admin DB and all 17 tables. Platform Temporal cluster. Provisioning saga with compensation chain. PostgreSQL shared cluster + PgBouncer. API gateway with tenant_identity routing and module activation enforcement. Auth service. Kafka + Schema Registry + DLQ. Redis cluster. Outbox processor. Quota counter infrastructure.


Phase 1 — Core Commerce (Weeks 3–6)

Unified catalog with item_type, tax_code_type, SAC/HSN codes, service fields. Item-type-aware order saga. Inventory reservation (Redis + pending_reservations). Slot reservation (Redis + pending_slot_reservations). Multi-tender payment session. Tax calculation service with HSN and SAC support. Basic notifications.


Phase 2 — Operations (Weeks 7–10)

Double-entry ledger. Three-tier reconciliation engine. Billing: POS (physical + service), invoicing, B2B credit. Procurement and GRN. Slot generation scheduler. Service completion flow. Service and subscription refund policies. Subscription system with catalog_item_id and service entitlements. Platform billing service with invoice retry lifecycle.


Phase 3 — Intelligence (Weeks 11–14)

All AI agents with circuit breakers. OpenSearch catalog (physical + service items). AI routing, demand forecasting, service booking recommendations. Fraud detection. KEDA autoscaling per topic. Tier promotion job.


Phase 4 — Seller Marketplace (Weeks 15–19)

Seller registration with KYC workflow. Seller catalog (physical + service listings). Annual fee with grace period. Commission engine with snapshot. Payout three-state lifecycle. Debit notes. Seller dashboard on analytics replica. Auto-suspension engine.


Phase 5 — Scale (Weeks 20–23)

Multi-currency + FX (Redis-cached, DB fallback). Multi-region catalog. ClickHouse analytics pipeline. Advanced dashboards. Data archival. Webhook dispatcher HMAC auth and retry queue. Credential auto-rotation.


20. Key Design Rules (Non-Negotiable)

All amounts in integer paise — no float arithmetic anywhere. All prices in DB are pre-tax base — display price derived at read time, never stored. item_type is checked before every inventory, slot, delivery, and tax operation. tax_mode is passed explicitly to every price calculation — never inferred from context. tenant_id is passed as an explicit parameter in every service method and Kafka header — never ThreadLocal in async code. SET LOCAL search_path on every transaction — never at connection open time. Ledger is append-only — no UPDATE or DELETE ever runs on ledger_entries. Compensation rates (commission, TCS) snapshotted at sale time — never read from current rate table at payout time. Provisioning always runs as a Temporal saga with compensation — never as a single "atomic" operation. Module activation sync always uses the outbox pattern — never a direct two-write sequence. Redis is a speed layer — every Redis operation has a defined DB fallback behaviour.

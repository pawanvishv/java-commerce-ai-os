// Switch to admin to authenticate
db = db.getSiblingDB('admin');
db.auth('commerce', 'commerce');

// catalog_web_commerce database
db = db.getSiblingDB('catalog_web_commerce');
db.createCollection('catalog_items');
db.catalog_items.createIndex({ tenant_id: "hashed" });
db.catalog_items.createIndex({ tenant_id: 1, status: 1, item_type: 1 });

// catalog_full_commerce database
db = db.getSiblingDB('catalog_full_commerce');
db.createCollection('catalog_items');
db.catalog_items.createIndex({ tenant_id: "hashed" });
db.catalog_items.createIndex({ tenant_id: 1, status: 1, item_type: 1 });

print('MongoDB catalog databases initialized');

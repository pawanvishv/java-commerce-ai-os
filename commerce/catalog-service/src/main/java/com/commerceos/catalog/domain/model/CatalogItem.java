package com.commerceos.catalog.domain.model;

import com.commerceos.catalog.domain.enums.ItemStatus;
import com.commerceos.catalog.domain.enums.ItemType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "catalog_items")
@CompoundIndexes({
    @CompoundIndex(
        name = "idx_tenant_status_type",
        def = "{'tenantId': 1, 'status': 1, 'itemType': 1}")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogItem {

    @Id
    private String id;

    private String tenantId;
    private String sku;
    private String name;
    private String description;
    private ItemType itemType;
    private ItemStatus status;
    private long basePricePaise;
    private String taxCode;
    private String taxCodeType;
    private String uom;
    private List<String> tags;
    private Map<String, String> attributes;
    private List<String> imageUrls;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    private String createdBy;
    private String updatedBy;
}

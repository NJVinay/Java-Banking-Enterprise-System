package com.enterprise.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory entity for inventory management system module.
 */
@Entity
@Table(name = "inventory_items", indexes = {
        @Index(name = "idx_sku", columnList = "sku"),
        @Index(name = "idx_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku; // Stock Keeping Unit

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantityInStock = 0;

    @Column(nullable = false)
    private Integer reorderLevel = 10;

    @Column(nullable = false)
    private Integer reorderQuantity = 50;

    @Column(length = 50)
    private String unit = "PIECE"; // PIECE, KG, LITER, etc.

    @Column(length = 50)
    private String supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastRestockedAt;

    @Version
    private Long version;

    public enum ItemStatus {
        ACTIVE, DISCONTINUED, OUT_OF_STOCK, BACKORDERED
    }

    public boolean needsReorder() {
        return quantityInStock <= reorderLevel;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantityInStock += quantity;
        this.lastRestockedAt = LocalDateTime.now();
        if (this.quantityInStock > 0 && status == ItemStatus.OUT_OF_STOCK) {
            status = ItemStatus.ACTIVE;
        }
    }

    public void removeStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > quantityInStock) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.quantityInStock -= quantity;
        if (this.quantityInStock == 0) {
            status = ItemStatus.OUT_OF_STOCK;
        }
    }

    public BigDecimal getTotalValue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantityInStock));
    }
}

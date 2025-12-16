package com.enterprise.banking.repository;

import com.enterprise.banking.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for InventoryItem entity.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Find item by SKU.
     */
    Optional<InventoryItem> findBySku(String sku);

    /**
     * Find items by category.
     */
    List<InventoryItem> findByCategory(String category);

    /**
     * Find items by brand.
     */
    List<InventoryItem> findByBrand(String brand);

    /**
     * Find items by status.
     */
    List<InventoryItem> findByStatus(InventoryItem.ItemStatus status);

    /**
     * Search items by name (case-insensitive partial match).
     */
    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<InventoryItem> searchByName(@Param("name") String name);

    /**
     * Find items that need reordering.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityInStock <= i.reorderLevel " +
            "AND i.status = 'ACTIVE'")
    List<InventoryItem> findItemsNeedingReorder();

    /**
     * Find out of stock items.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityInStock = 0")
    List<InventoryItem> findOutOfStockItems();

    /**
     * Find items with low stock (below reorder level).
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantityInStock > 0 " +
            "AND i.quantityInStock <= i.reorderLevel")
    List<InventoryItem> findLowStockItems();

    /**
     * Get total inventory value by category.
     */
    @Query("SELECT i.category, SUM(i.unitPrice * i.quantityInStock) FROM InventoryItem i " +
            "GROUP BY i.category")
    List<Object[]> getTotalValueByCategory();

    /**
     * Find items by supplier.
     */
    List<InventoryItem> findBySupplier(String supplier);

    /**
     * Get all unique categories.
     */
    @Query("SELECT DISTINCT i.category FROM InventoryItem i ORDER BY i.category")
    List<String> findAllCategories();

    /**
     * Get all unique brands.
     */
    @Query("SELECT DISTINCT i.brand FROM InventoryItem i WHERE i.brand IS NOT NULL ORDER BY i.brand")
    List<String> findAllBrands();

    /**
     * Check if SKU exists.
     */
    boolean existsBySku(String sku);

    /**
     * Count items by category.
     */
    long countByCategory(String category);

    /**
     * Get total inventory value.
     */
    @Query("SELECT SUM(i.unitPrice * i.quantityInStock) FROM InventoryItem i")
    BigDecimal getTotalInventoryValue();
}

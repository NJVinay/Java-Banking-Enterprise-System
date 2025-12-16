package com.enterprise.banking.controller;

import com.enterprise.banking.model.InventoryItem;
import com.enterprise.banking.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for inventory management operations (MVC Pattern - Controller
 * layer).
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Add a new inventory item.
     * POST /api/inventory/items
     */
    @PostMapping("/items")
    public ResponseEntity<InventoryItem> addItem(@RequestBody InventoryItem item) {
        log.info("REST API - Add inventory item: {}", item.getName());

        try {
            InventoryItem savedItem = inventoryService.addItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (IllegalArgumentException e) {
            log.error("Add item failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Add item error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add stock to an item.
     * POST /api/inventory/items/{sku}/add-stock
     */
    @PostMapping("/items/{sku}/add-stock")
    public ResponseEntity<Void> addStock(
            @PathVariable String sku,
            @RequestParam int quantity) {

        log.info("REST API - Add stock: {} units to SKU: {}", quantity, sku);

        try {
            inventoryService.addStock(sku, quantity);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Add stock failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Add stock error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove stock from an item.
     * POST /api/inventory/items/{sku}/remove-stock
     */
    @PostMapping("/items/{sku}/remove-stock")
    public ResponseEntity<Void> removeStock(
            @PathVariable String sku,
            @RequestParam int quantity) {

        log.info("REST API - Remove stock: {} units from SKU: {}", quantity, sku);

        try {
            inventoryService.removeStock(sku, quantity);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Remove stock failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Remove stock error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get items needing reorder.
     * GET /api/inventory/items/reorder
     */
    @GetMapping("/items/reorder")
    public ResponseEntity<List<InventoryItem>> getItemsNeedingReorder() {
        log.info("REST API - Get items needing reorder");

        try {
            List<InventoryItem> items = inventoryService.getItemsNeedingReorder();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Get reorder items error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get low stock items.
     * GET /api/inventory/items/low-stock
     */
    @GetMapping("/items/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        log.info("REST API - Get low stock items");

        try {
            List<InventoryItem> items = inventoryService.getLowStockItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Get low stock items error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total inventory value.
     * GET /api/inventory/total-value
     */
    @GetMapping("/total-value")
    public ResponseEntity<BigDecimal> getTotalInventoryValue() {
        log.info("REST API - Get total inventory value");

        try {
            BigDecimal totalValue = inventoryService.getTotalInventoryValue();
            return ResponseEntity.ok(totalValue);
        } catch (Exception e) {
            log.error("Get total inventory value error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get inventory summary by category.
     * GET /api/inventory/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getInventorySummary() {
        log.info("REST API - Get inventory summary");

        try {
            Map<String, Long> summary = inventoryService.getInventorySummaryByCategory();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Get inventory summary error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search inventory items.
     * GET /api/inventory/items/search
     */
    @GetMapping("/items/search")
    public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String keyword) {
        log.info("REST API - Search items with keyword: {}", keyword);

        try {
            List<InventoryItem> items = inventoryService.searchItems(keyword);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("Search items error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

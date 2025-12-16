package com.enterprise.banking.service;

import com.enterprise.banking.model.InventoryItem;
import com.enterprise.banking.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for inventory management operations.
 * Demonstrates Java 8 Streams, Lambda expressions, and Collections Framework.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryItemRepository inventoryRepository;
    
    @Transactional
    public InventoryItem addItem(InventoryItem item) {
        log.info("Adding new inventory item: {}", item.getName());
        
        if (inventoryRepository.existsBySku(item.getSku())) {
            throw new IllegalArgumentException("Item with SKU already exists: " + item.getSku());
        }
        
        return inventoryRepository.save(item);
    }
    
    @Transactional
    public void addStock(String sku, int quantity) {
        log.info("Adding stock: {} units to SKU: {}", quantity, sku);
        
        InventoryItem item = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + sku));
        
        item.addStock(quantity);
        inventoryRepository.save(item);
        
        log.info("Stock added successfully. New quantity: {}", item.getQuantityInStock());
    }
    
    @Transactional
    public void removeStock(String sku, int quantity) {
        log.info("Removing stock: {} units from SKU: {}", quantity, sku);
        
        InventoryItem item = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + sku));
        
        item.removeStock(quantity);
        inventoryRepository.save(item);
        
        log.info("Stock removed successfully. Remaining quantity: {}", item.getQuantityInStock());
    }
    
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemsNeedingReorder() {
        return inventoryRepository.findItemsNeedingReorder();
    }
    
    @Transactional(readOnly = true)
    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        return inventoryRepository.getTotalInventoryValue();
    }
    
    /**
     * Get inventory summary using Java 8 Streams and Collections.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getInventorySummaryByCategory() {
        return inventoryRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                InventoryItem::getCategory,
                Collectors.counting()
            ));
    }
    
    /**
     * Get total value by category using Streams and Lambda.
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getTotalValueByCategory() {
        return inventoryRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                InventoryItem::getCategory,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    InventoryItem::getTotalValue,
                    BigDecimal::add
                )
            ));
    }
    
    @Transactional(readOnly = true)
    public List<InventoryItem> searchItems(String keyword) {
        return inventoryRepository.searchByName(keyword);
    }
}

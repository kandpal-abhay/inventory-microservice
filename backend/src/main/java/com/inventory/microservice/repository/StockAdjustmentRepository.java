package com.inventory.microservice.repository;

import com.inventory.microservice.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    List<StockAdjustment> findByProductId(Long productId);

    List<StockAdjustment> findByProductSku(String productSku);

    List<StockAdjustment> findByAdjustmentType(String adjustmentType);

    List<StockAdjustment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

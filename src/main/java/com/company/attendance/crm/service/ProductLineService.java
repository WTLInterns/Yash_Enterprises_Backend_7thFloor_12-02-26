package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.ProductLineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductLineService {
    private final DealRepository dealRepository;
    private final ProductLineRepository productLineRepository;

    public ProductLineService(DealRepository dealRepository, ProductLineRepository productLineRepository) {
        this.dealRepository = dealRepository;
        this.productLineRepository = productLineRepository;
    }

    public List<ProductLine> list(UUID dealId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        return productLineRepository.findByDeal(deal);
    }

    public ProductLine create(UUID dealId, ProductLine pl, UUID userId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        pl.setDeal(deal);
        pl.setCreatedBy(userId);
        pl.computeTotal();
        return productLineRepository.save(pl);
    }

    public ProductLine update(UUID dealId, UUID productId, ProductLine incoming){
        ProductLine db = productLineRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("Product not in deal");
        db.setProductName(incoming.getProductName());
        db.setListPrice(incoming.getListPrice());
        db.setQuantity(incoming.getQuantity());
        db.setDiscount(incoming.getDiscount());
        db.computeTotal();
        return productLineRepository.save(db);
    }

    public void delete(UUID dealId, UUID productId){
        ProductLine db = productLineRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("Product not in deal");
        productLineRepository.delete(db);
    }

    public BigDecimal grandTotal(UUID dealId){
        return list(dealId).stream()
                .map(pl -> pl.getTotal() != null ? pl.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.ProductLine;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.ProductLineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductLineService {
    private final DealRepository dealRepository;
    private final ProductLineRepository productLineRepository;

    public ProductLineService(DealRepository dealRepository, ProductLineRepository productLineRepository) {
        this.dealRepository = dealRepository;
        this.productLineRepository = productLineRepository;
    }

    public List<ProductLine> list(Long dealId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        return productLineRepository.findByDeal(deal);
    }

    public ProductLine create(Long dealId, ProductLine pl, Integer userId){
        Deal deal = dealRepository.findByIdSafe(dealId);
        pl.setDeal(deal);
        pl.setCreatedBy(userId);
        pl.computeTotal();
        return productLineRepository.save(pl);
    }

    public ProductLine update(Long dealId, Integer productId, ProductLine incoming){
        ProductLine db = productLineRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (db.getDeal() == null || db.getDeal().getId() == null || !db.getDeal().getId().equals(dealId)) {
            throw new IllegalArgumentException("Product not in deal");
        }
        db.setProductName(incoming.getProductName());
        db.setListPrice(incoming.getListPrice());
        db.setQuantity(incoming.getQuantity());
        db.setDiscount(incoming.getDiscount());
        db.computeTotal();
        return productLineRepository.save(db);
    }

    public void delete(Long dealId, Integer productId){
        ProductLine db = productLineRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (db.getDeal() == null || db.getDeal().getId() == null || !db.getDeal().getId().equals(dealId)) {
            throw new IllegalArgumentException("Product not in deal");
        }
        productLineRepository.delete(db);
    }

    public BigDecimal grandTotal(Long dealId){
        return list(dealId).stream()
                .map(pl -> pl.getTotal() != null ? pl.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

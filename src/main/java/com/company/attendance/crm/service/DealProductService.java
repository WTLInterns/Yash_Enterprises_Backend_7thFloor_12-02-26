package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DealProductService {
    private final DealRepository dealRepository;
    private final ProductRepository productRepository;
    private final DealProductRepository dealProductRepository;

    public DealProductService(DealRepository dealRepository, ProductRepository productRepository, DealProductRepository dealProductRepository) {
        this.dealRepository = dealRepository;
        this.productRepository = productRepository;
        this.dealProductRepository = dealProductRepository;
    }

    public List<DealProduct> list(Long dealId){
        Deal deal = dealRepository.findByIdSafe(dealId.intValue());
        return dealProductRepository.findByDeal(deal);
    }

    public DealProduct create(Long dealId, Long productId, DealProduct incoming){
        Deal deal = dealRepository.findByIdSafe(dealId.intValue());
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        DealProduct dp = new DealProduct();
        dp.setDeal(deal);
        dp.setProduct(product);
        // snapshot price: use incoming if provided else product.price
        BigDecimal price = incoming.getUnitPrice() != null ? incoming.getUnitPrice() : product.getPrice();
        dp.setUnitPrice(price);
        dp.setQuantity(incoming.getQuantity());
        dp.setDiscount(incoming.getDiscount());
        dp.setTax(incoming.getTax());
        dp.computeTotal();
        return dealProductRepository.save(dp);
    }

    public DealProduct update(Long dealId, Long dealProductId, DealProduct incoming){
        DealProduct db = dealProductRepository.findById(dealProductId.intValue()).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId.intValue())) throw new IllegalArgumentException("DealProduct not in deal");
        // we allow editing quantity/discount/tax and unitPrice if needed (but it's snapshot semantics)
        if (incoming.getUnitPrice() != null) db.setUnitPrice(incoming.getUnitPrice());
        db.setQuantity(incoming.getQuantity());
        db.setDiscount(incoming.getDiscount());
        db.setTax(incoming.getTax());
        db.computeTotal();
        return dealProductRepository.save(db);
    }

    public void delete(Long dealId, Long dealProductId){
        DealProduct db = dealProductRepository.findById(dealProductId.intValue()).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId.intValue())) throw new IllegalArgumentException("DealProduct not in deal");
        dealProductRepository.delete(db);
    }
}

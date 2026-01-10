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
import java.util.UUID;

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

    public List<DealProduct> list(UUID dealId){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        // Compatibility lookup for legacy schemas storing deal_id in padded BINARY(36)
        return dealProductRepository.findByDealIdCompat(deal.getId().toString());
    }

    public DealProduct create(UUID dealId, UUID productId, DealProduct incoming){
        Deal deal = dealRepository.findByIdSafe(dealId).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        DealProduct dp = new DealProduct();
        dp.setDeal(deal);
        dp.setProduct(product);
        // snapshot price: use incoming if provided else product.unitPrice
        BigDecimal price = incoming.getUnitPrice() != null ? incoming.getUnitPrice() : product.getUnitPrice();
        dp.setUnitPrice(price);
        dp.setQuantity(incoming.getQuantity());
        dp.setDiscount(incoming.getDiscount());
        dp.setTax(incoming.getTax());
        dp.computeTotal();
        return dealProductRepository.save(dp);
    }

    public DealProduct update(UUID dealId, UUID dealProductId, DealProduct incoming){
        DealProduct db = dealProductRepository.findById(dealProductId).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("DealProduct not in deal");
        // we allow editing quantity/discount/tax and unitPrice if needed (but it's snapshot semantics)
        if (incoming.getUnitPrice() != null) db.setUnitPrice(incoming.getUnitPrice());
        db.setQuantity(incoming.getQuantity());
        db.setDiscount(incoming.getDiscount());
        db.setTax(incoming.getTax());
        db.computeTotal();
        return dealProductRepository.save(db);
    }

    public void delete(UUID dealId, UUID dealProductId){
        DealProduct db = dealProductRepository.findById(dealProductId).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId)) throw new IllegalArgumentException("DealProduct not in deal");
        dealProductRepository.delete(db);
    }
}

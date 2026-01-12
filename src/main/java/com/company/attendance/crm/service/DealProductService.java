package com.company.attendance.crm.service;

import com.company.attendance.crm.dto.DealProductDto;
import com.company.attendance.crm.dto.DealProductRequestDto;
import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

    private DealProductDto toDto(DealProduct dp) {
        if (dp == null) return null;
        DealProductDto dto = new DealProductDto();
        dto.setId(dp.getId());
        dto.setDealId(dp.getDeal() != null ? dp.getDeal().getId().longValue() : null);
        dto.setProductId(dp.getProduct() != null ? dp.getProduct().getId() : null);
        dto.setProductName(dp.getProduct() != null ? dp.getProduct().getName() : null);
        dto.setUnitPrice(dp.getUnitPrice());
        dto.setQuantity(dp.getQuantity());
        dto.setDiscount(dp.getDiscount());
        dto.setTax(dp.getTax());
        dto.setTotal(dp.getTotal());
        dto.setCreatedAt(dp.getCreatedAt());
        dto.setUpdatedAt(dp.getUpdatedAt());
        return dto;
    }

    public List<DealProductDto> list(Long dealId){
        Deal deal = dealRepository.findByIdSafe(dealId.intValue());
        return dealProductRepository.findByDeal(deal)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public DealProductDto create(Long dealId, DealProductRequestDto incoming){
        if (incoming == null || incoming.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }

        Deal deal = dealRepository.findByIdSafe(dealId.intValue());
        Product product = productRepository.findById(incoming.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
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
        return toDto(dealProductRepository.save(dp));
    }

    public DealProductDto update(Long dealId, Long dealProductId, DealProductRequestDto incoming){
        DealProduct db = dealProductRepository.findById(dealProductId.intValue()).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId.intValue())) throw new IllegalArgumentException("DealProduct not in deal");
        // we allow editing quantity/discount/tax and unitPrice if needed (but it's snapshot semantics)
        if (incoming != null && incoming.getUnitPrice() != null) db.setUnitPrice(incoming.getUnitPrice());
        if (incoming != null) {
            db.setQuantity(incoming.getQuantity());
            db.setDiscount(incoming.getDiscount());
            db.setTax(incoming.getTax());
        }
        db.computeTotal();
        return toDto(dealProductRepository.save(db));
    }

    public void delete(Long dealId, Long dealProductId){
        DealProduct db = dealProductRepository.findById(dealProductId.intValue()).orElseThrow(() -> new IllegalArgumentException("DealProduct not found"));
        if (!db.getDeal().getId().equals(dealId.intValue())) throw new IllegalArgumentException("DealProduct not in deal");
        dealProductRepository.delete(db);
    }
}

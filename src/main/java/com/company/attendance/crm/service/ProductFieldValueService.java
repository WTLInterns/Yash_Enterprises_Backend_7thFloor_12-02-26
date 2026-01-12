package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.ProductFieldDefinition;
import com.company.attendance.crm.entity.ProductFieldValue;
import com.company.attendance.crm.repository.ProductFieldDefinitionRepository;
import com.company.attendance.crm.repository.ProductFieldValueRepository;
import com.company.attendance.crm.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFieldValueService {
    private final ProductRepository productRepository;
    private final ProductFieldDefinitionRepository defRepository;
    private final ProductFieldValueRepository valueRepository;

    public ProductFieldValueService(ProductRepository productRepository,
                                    ProductFieldDefinitionRepository defRepository,
                                    ProductFieldValueRepository valueRepository) {
        this.productRepository = productRepository;
        this.defRepository = defRepository;
        this.valueRepository = valueRepository;
    }

    public List<ProductFieldValue> list(Long productId){
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return valueRepository.findByProduct(product);
    }

    public ProductFieldValue upsert(Long productId, String fieldKey, String value){
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductFieldDefinition def = defRepository.findByFieldKey(fieldKey).orElseThrow(() -> new IllegalArgumentException("Field definition not found for key: "+fieldKey));
        ProductFieldValue existing = valueRepository.findByProductAndFieldDefinition(product, def).orElse(null);
        if (existing == null){
            ProductFieldValue v = new ProductFieldValue();
            v.setProduct(product);
            v.setFieldDefinition(def);
            v.setValue(value);
            return valueRepository.save(v);
        } else {
            existing.setValue(value);
            return valueRepository.save(existing);
        }
    }
}

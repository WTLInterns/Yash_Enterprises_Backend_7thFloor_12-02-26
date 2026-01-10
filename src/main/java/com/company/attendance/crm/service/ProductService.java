package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.entity.ProductPriceHistory;
import com.company.attendance.crm.repository.ProductRepository;
import com.company.attendance.crm.repository.ProductSpecifications;
import com.company.attendance.crm.repository.ProductPriceHistoryRepository;
import com.company.attendance.crm.repository.ProductCategoryRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.exception.ResourceNotFoundException;
import com.company.attendance.exception.InvalidForeignKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductPriceHistoryRepository priceHistoryRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product create(Product product){
        if (product.getProductName() == null || product.getProductName().isBlank()){
            throw new IllegalArgumentException("productName is required");
        }
        if (productRepository.existsByProductNameIgnoreCase(product.getProductName())){
            throw new IllegalArgumentException("Product name already exists");
        }
        return productRepository.save(product);
    }

    public Page<Product> list(Pageable pageable){
        return productRepository.findAll(pageable);
    }

    public Page<Product> search(Boolean active, String category, UUID ownerId, String q, UUID categoryId, Pageable pageable){
        Specification<Product> spec = Specification.where(ProductSpecifications.active(active))
                .and(ProductSpecifications.category(category))
                .and(ProductSpecifications.owner(ownerId))
                .and(ProductSpecifications.nameOrCodeContains(q))
                .and(ProductSpecifications.categoryId(categoryId));
        return productRepository.findAll(spec, pageable);
    }

    public Optional<Product> get(UUID id){
        return productRepository.findById(id);
    }

    public Product update(UUID id, Product incoming){
        // Find existing product
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Validate foreign key exists
        if (incoming.getCategoryId() != null && !categoryRepository.existsById(incoming.getCategoryId())) {
            throw new InvalidForeignKeyException("Category not found with ID: " + incoming.getCategoryId());
        }
        
        // Note: ownerId is a UUID and should be sourced from authentication; no FK validation against Employee (Long id).
        
        // Check for duplicate product name (excluding current product)
        if (!existing.getProductName().equals(incoming.getProductName()) && 
            productRepository.existsByProductNameIgnoreCase(incoming.getProductName())) {
            throw new IllegalArgumentException("Product name already exists");
        }
        
        BigDecimal oldPrice = existing.getUnitPrice();
        
        // Update only editable fields, keep system fields intact
        existing.setProductName(incoming.getProductName());
        existing.setProductCode(incoming.getProductCode());
        // Note: productCategory field is not used in frontend, categoryId is used instead
        // existing.setProductCategory(incoming.getProductCategory());
        existing.setUnitPrice(incoming.getUnitPrice());
        existing.setDescription(incoming.getDescription());
        existing.setOwnerId(incoming.getOwnerId());
        existing.setCategoryId(incoming.getCategoryId());
        
        // Keep createdAt intact, update updatedAt
        // existing.setCreatedAt() - Don't touch this
        // existing.setUpdatedAt() - This will be handled by @PreUpdate
        
        if (incoming.getActive() != null) existing.setActive(incoming.getActive());
        
        Product saved = productRepository.save(existing);
        
        // Record price history if changed
        if (incoming.getUnitPrice() != null && (oldPrice == null || oldPrice.compareTo(incoming.getUnitPrice()) != 0)){
            ProductPriceHistory ph = new ProductPriceHistory();
            ph.setProduct(saved);
            ph.setOldPrice(oldPrice);
            ph.setNewPrice(incoming.getUnitPrice());
            priceHistoryRepository.save(ph);
        }
        return saved;
    }

    public Product patchStatus(UUID id, boolean active){
        Product db = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        db.setActive(active);
        return productRepository.save(db);
    }

    public void delete(UUID id){
        productRepository.deleteById(id);
    }

    public void bulkPatchStatus(Iterable<UUID> ids, boolean active){
        ids.forEach(id -> {
            productRepository.findById(id).ifPresent(p -> {
                p.setActive(active);
                productRepository.save(p);
            });
        });
    }

    public void bulkDelete(Iterable<UUID> ids){
        ids.forEach(productRepository::deleteById);
    }
}

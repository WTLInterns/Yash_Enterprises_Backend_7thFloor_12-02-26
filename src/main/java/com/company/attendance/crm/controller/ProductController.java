package com.company.attendance.crm.controller;

import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.service.ProductService;
import com.company.attendance.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.company.attendance.crm.entity.DealProduct;
import com.company.attendance.crm.entity.Product;
import com.company.attendance.crm.repository.DealProductRepository;
import com.company.attendance.crm.repository.ProductPriceHistoryRepository;
import com.company.attendance.crm.repository.ProductRepository;
import com.company.attendance.crm.entity.ProductPriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final DealProductRepository dealProductRepository;
    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;

    public ProductController(ProductService productService,
                             DealProductRepository dealProductRepository,
                             ProductRepository productRepository,
                             ProductPriceHistoryRepository priceHistoryRepository) {
        this.productService = productService;
        this.dealProductRepository = dealProductRepository;
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product){
        Product created = productService.create(product);
        return ResponseEntity.created(URI.create("/api/products/"+created.getId())).body(created);
    }

    @GetMapping
    public Page<Product> list(@RequestParam(value = "active", required = false) Boolean active,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "ownerId", required = false) java.util.UUID ownerId,
                              @RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "categoryId", required = false) java.util.UUID categoryId,
                              Pageable pageable){
        // Default to active=true unless explicitly requested otherwise
        Boolean effectiveActive = (active == null ? Boolean.TRUE : active);
        if (active == null && category == null && ownerId == null && q == null && categoryId == null) {
            return productService.list(pageable);
        }
        return productService.search(effectiveActive, category, ownerId, q, categoryId, pageable);
    }

    @GetMapping("/search")
    public Page<Product> search(@RequestParam(value = "active", required = false) Boolean active,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "ownerId", required = false) java.util.UUID ownerId,
                                @RequestParam(value = "q", required = false) String q,
                                @RequestParam(value = "categoryId", required = false) java.util.UUID categoryId,
                                Pageable pageable){
        // Default to active=true unless explicitly requested otherwise
        Boolean effectiveActive = (active == null ? Boolean.TRUE : active);
        return productService.search(effectiveActive, category, ownerId, q, categoryId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable UUID id){
        Product product = productService.get(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable UUID id, @RequestBody Product incoming){
        return productService.update(id, incoming);
    }

    @PatchMapping("/{id}/status")
    public Product patchStatus(@PathVariable UUID id, @RequestParam("active") boolean active){
        return productService.patchStatus(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Bulk operations
    @PatchMapping("/bulk/status")
    public ResponseEntity<Void> bulkStatus(@RequestBody java.util.Map<String, Object> body){
        java.util.List<java.util.UUID> ids = (java.util.List<java.util.UUID>) body.get("productIds");
        boolean active = Boolean.TRUE.equals(body.get("active"));
        productService.bulkPatchStatus(ids, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDelete(@RequestBody java.util.Map<String, java.util.List<java.util.UUID>> body){
        java.util.List<java.util.UUID> ids = body.get("productIds");
        productService.bulkDelete(ids);
        return ResponseEntity.noContent().build();
    }

    // Usage: in which deals is this product used
    @GetMapping("/{productId}/deals")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> productDeals(@PathVariable UUID productId){
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        java.util.List<DealProduct> lines = dealProductRepository.findByProduct(product);
        java.util.List<java.util.Map<String, Object>> resp = new java.util.ArrayList<>();
        for (DealProduct dp : lines){
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("dealId", dp.getDeal().getId());
            row.put("dealName", dp.getDeal().getName());
            row.put("quantity", dp.getQuantity());
            row.put("total", dp.getTotal());
            resp.add(row);
        }
        return ResponseEntity.ok(resp);
    }

    // Price history
    @GetMapping("/{productId}/price-history")
    public ResponseEntity<java.util.List<ProductPriceHistory>> priceHistory(@PathVariable UUID productId){
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(priceHistoryRepository.findByProductOrderByChangedAtDesc(product));
    }

    // Permissions helper (simple)
    @GetMapping("/{id}/permissions")
    public ResponseEntity<java.util.Map<String, Boolean>> permissions(@PathVariable UUID id,
                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                            @RequestHeader(value = "X-User-Id", required = false) java.util.UUID userId){
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean isOwner = userId != null && userId.equals(p.getOwnerId());
        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
        map.put("canEdit", isAdmin || isOwner);
        map.put("canDelete", isAdmin);
        map.put("canDisable", isAdmin);
        return ResponseEntity.ok(map);
    }
}

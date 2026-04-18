package com.example.jpademo.service;

import com.example.jpademo.entity.Category;
import com.example.jpademo.entity.Product;
import com.example.jpademo.repository.CategoryRepository;
import com.example.jpademo.repository.ProductRepository;
import com.example.jpademo.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Product createProduct(String name, Double price,
                                  String description, Integer stock,
                                  String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
            .orElseGet(() -> categoryRepository.save(
                new Category(null, categoryName, null)));

        Product product = new Product(null, name, price, description, stock, category);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(Double min, Double max) {
        return productRepository.findProductsInPriceRange(min, max);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsPaginated(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> filterProducts(Double minPrice, Double maxPrice,
                                         String category, String keyword,
                                         int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Specification<Product> spec =
            ProductSpecifications.withFilters(minPrice, maxPrice, category, keyword);
        return productRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStock(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
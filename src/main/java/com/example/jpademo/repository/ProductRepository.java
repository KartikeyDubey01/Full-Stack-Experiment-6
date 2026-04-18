package com.example.jpademo.repository;

import com.example.jpademo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    // Derived query — price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // JPQL — price range with explicit sort
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max ORDER BY p.price ASC")
    List<Product> findProductsInPriceRange(@Param("min") Double min,
                                           @Param("max") Double max);

    // JPQL — products belonging to a category name, paginated
    @Query("SELECT p FROM Product p WHERE p.category.name = :catName")
    Page<Product> findByCategoryName(@Param("catName") String catName,
                                     Pageable pageable);

    // Derived — products by category id, sorted by price desc
    List<Product> findByCategoryIdOrderByPriceDesc(Long categoryId);

    // Native SQL — low stock alert
    @Query(value = "SELECT * FROM products WHERE stock < :threshold",
           nativeQuery = true)
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
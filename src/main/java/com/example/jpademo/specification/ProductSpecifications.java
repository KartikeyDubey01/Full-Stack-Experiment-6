package com.example.jpademo.specification;

import com.example.jpademo.entity.Category;
import com.example.jpademo.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    // Dynamic Criteria API filter — price range + category + keyword
    public static Specification<Product> withFilters(
            Double minPrice,
            Double maxPrice,
            String categoryName,
            String keyword) {

        return (Root<Product> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (categoryName != null && !categoryName.isBlank()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("name"), categoryName));
            }

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
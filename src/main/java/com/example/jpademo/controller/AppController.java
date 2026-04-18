package com.example.jpademo.controller;

import com.example.jpademo.entity.Product;
import com.example.jpademo.entity.User;
import com.example.jpademo.service.ProductService;
import com.example.jpademo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppController {

    private final ProductService productService;
    private final UserService userService;

    // ── USER endpoints ─────────────────────────────────────

    // POST /api/users
    // Body: {"username":"alice","email":"alice@test.com","roles":["ROLE_ADMIN"]}
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody Map<String, Object> body) {
        User user = userService.createUserWithRoles(
            (String) body.get("username"),
            (String) body.get("email"),
            (List<String>) body.get("roles")
        );
        return ResponseEntity.ok(user);
    }

    // GET /api/users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/by-role?role=ROLE_ADMIN
    @GetMapping("/users/by-role")
    public ResponseEntity<List<User>> getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // GET /api/users/search?keyword=ali&page=0&size=5
    @GetMapping("/users/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.searchUsers(keyword, page, size));
    }

    // ── PRODUCT endpoints ──────────────────────────────────

    // POST /api/products
    // Body: {"name":"Laptop","price":75000,"description":"16GB RAM","stock":10,"category":"Electronics"}
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> body) {
        Product product = productService.createProduct(
            (String)  body.get("name"),
            ((Number) body.get("price")).doubleValue(),
            (String)  body.get("description"),
            ((Number) body.get("stock")).intValue(),
            (String)  body.get("category")
        );
        return ResponseEntity.ok(product);
    }

    // GET /api/products
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET /api/products/price-range?min=100&max=5000
    @GetMapping("/products/price-range")
    public ResponseEntity<List<Product>> getByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(productService.getProductsByPriceRange(min, max));
    }

    // GET /api/products/filter?minPrice=500&maxPrice=80000&category=Electronics&keyword=lap&page=0&size=5
    @GetMapping("/products/filter")
    public ResponseEntity<Page<Product>> filterProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            productService.filterProducts(minPrice, maxPrice, category, keyword, page, size));
    }

    // GET /api/products/paginated?page=0&size=5&sortBy=price
    @GetMapping("/products/paginated")
    public ResponseEntity<Page<Product>> getPaginated(
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "5")     int size,
            @RequestParam(defaultValue = "price") String sortBy) {
        return ResponseEntity.ok(productService.getProductsPaginated(page, size, sortBy));
    }

    // GET /api/products/low-stock?threshold=20
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStock(
            @RequestParam(defaultValue = "20") int threshold) {
        return ResponseEntity.ok(productService.getLowStock(threshold));
    }
}
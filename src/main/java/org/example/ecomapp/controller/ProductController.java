package org.example.ecomapp.controller;

import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.exception.CustomException;
import org.example.ecomapp.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> fetchProducts() {
        return ResponseEntity.ok(productService.fetchProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchProduct(@PathVariable Long id) {
        try {
            ProductResponse response = productService.fetchProduct(id);
            return ResponseEntity.ok(response);
        } catch (CustomException exception) {
            String message = exception.getMessage();

            if (message.endsWith("was not found.")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", message));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", message));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword)
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @RequestBody ProductRequest request
    ) {
        ProductResponse createdProduct = productService.addProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        ProductResponse updatedProduct =
                productService.updateProduct(id, request);

        if (updatedProduct == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        Boolean deactivated = productService.deleteProduct(id);

        if (deactivated) {
            return ResponseEntity.ok(
                    "Product ID " + id + " deactivated."
            );
        }

        return ResponseEntity.notFound().build();
    }
}
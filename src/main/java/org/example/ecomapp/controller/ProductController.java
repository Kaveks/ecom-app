package org.example.ecomapp.controller;


import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public ResponseEntity<List<ProductResponse>> fetchProduct(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.fetchProducts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> fetchProduct(@PathVariable Long id){
        ProductResponse resp=productService.fetchProduct(id);
        if(resp ==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }


    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request){
        ProductResponse createdProduct=productService.addProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request){
        ProductResponse updatedProduct =productService.updateProduct(id,request);
        if(updatedProduct == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedProduct);

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        Boolean deleted= productService.deleteProduct(id);
        if (deleted){
            return ResponseEntity.ok("Product id: " + id + " deleted");
        }
        return ResponseEntity.notFound().build();
    }
}

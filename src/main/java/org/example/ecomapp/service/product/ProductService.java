package org.example.ecomapp.service.product;

import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.exception.CustomException;
import org.example.ecomapp.model.Inventory;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.util.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    private Product toProductEntity(ProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());

        return product;
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(String.valueOf(product.getId()));
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setCategory(product.getCategory());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());

        int availableStock = inventoryRepository
                .findByProduct_Id(product.getId())
                .map(Inventory::getAvailableStock)
                .orElse(0);

        response.setAvailableStock(availableStock);

        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponse fetchProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "Product with ID " + id + " was not found."
                ));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new CustomException(
                    "Product '" + product.getName()
                            + "' is currently unavailable."
            );
        }

        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> fetchProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        Product product = toProductEntity(request);
        Product savedProduct = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantityOnHand(0);
        inventory.setQuantityReserved(0);

        inventoryRepository.save(inventory);

        return toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {
        Product existingProduct = productRepository.findById(id)
                .orElse(null);

        if (existingProduct == null) {
            return null;
        }

        if (FieldUtils.hasText(request, ProductRequest::getName)) {
            existingProduct.setName(request.getName());
        }

        if (FieldUtils.hasValue(request, ProductRequest::getPrice)) {
            existingProduct.setPrice(request.getPrice());
        }

        if (FieldUtils.hasValue(request, ProductRequest::getCategory)) {
            existingProduct.setCategory(request.getCategory());
        }

        if (FieldUtils.hasText(request, ProductRequest::getDescription)) {
            existingProduct.setDescription(request.getDescription());
        }

        if (FieldUtils.hasText(request, ProductRequest::getImageUrl)) {
            existingProduct.setImageUrl(request.getImageUrl());
        }

        Product savedProduct = productRepository.save(existingProduct);

        return toProductResponse(savedProduct);
    }

    @Transactional
    public Boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return false;
        }

        product.setActive(false);
        productRepository.save(product);

        return true;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }
}
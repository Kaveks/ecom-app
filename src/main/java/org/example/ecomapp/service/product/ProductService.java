package org.example.ecomapp.service.product;

import org.example.ecomapp.dto.product.InventoryResponse;
import org.example.ecomapp.dto.product.InventoryStockRequest;
import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.enums.InventoryMovementType;
import org.example.ecomapp.exception.CustomException;
import org.example.ecomapp.model.Inventory;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.service.inventory.InventoryService;
import org.example.ecomapp.util.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    public ProductService(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            InventoryService inventoryService
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService=inventoryService;
    }

    // product
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
    //inventory
    @Transactional(readOnly = true)
    public InventoryResponse fetchInventory(Long productId) {
        return toInventoryResponse(inventoryService.fetchInventory(productId));
    }

    @Transactional
    public InventoryResponse receiveStock(
            Long productId,
            InventoryStockRequest request
    ) {

        inventoryService.processStock(
                productId,
                request.getQuantity(),
                InventoryMovementType.STOCK_RECEIVED,
                request.getReason()
        );

        return toInventoryResponse(inventoryService.fetchInventory(productId));
    }

    @Transactional
    public InventoryResponse adjustStock(
            Long productId,
            InventoryStockRequest request
    ) {

        inventoryService.processStock(
                productId,
                request.getQuantity(),
                InventoryMovementType.STOCK_ADJUSTED,
                request.getReason()
        );

        return toInventoryResponse(inventoryService.fetchInventory(productId));
    }


    private InventoryResponse toInventoryResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();

        response.setProductId(
                String.valueOf(inventory.getProduct().getId())
        );
        response.setProductName(inventory.getProduct().getName());
        response.setQuantityOnHand(inventory.getQuantityOnHand());
        response.setQuantityReserved(inventory.getQuantityReserved());
        response.setAvailableStock(inventory.getAvailableStock());

        return response;
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
}
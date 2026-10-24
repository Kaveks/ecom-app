package org.example.ecomapp.service.product;

import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    private Product toProductEntity(ProductRequest request){
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        return product;

    }
    private ProductResponse toProductResponse(Product product){
        ProductResponse response =new ProductResponse();
        response.setId(String.valueOf(product.getId()));
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setCategory(product.getCategory());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        return response;

    }
    private Boolean hasText(String val){
        return val !=null && !val.isBlank();
    }
    public ProductResponse fetchProduct(Long id){
        return productRepository.findById(id)
                .map(this::toProductResponse)
                .orElse(null);
    }
    public List<ProductResponse> fetchProducts(){
        return productRepository.findAll()
                .stream()
                .map(this::toProductResponse)
                .toList();

    }
    public ProductResponse addProduct(ProductRequest request){
        Product product =toProductEntity(request);
        Product savedProduct=productRepository.save(product);
        return toProductResponse(savedProduct);
    }
    public ProductResponse updateProduct(Long id , ProductRequest request){
        Product existingProduct = productRepository.findById(id).orElse(null);
        if(existingProduct==null){
            return null;
        }
        // update product
        if(hasText(request.getName())){
            existingProduct.setName(request.getName());
        }

        if(hasText(String.valueOf(request.getPrice()))){
            existingProduct.setPrice(request.getPrice());
        }

        if(hasText(String.valueOf(request.getQuantity()))){
            existingProduct.setQuantity(request.getQuantity());
        }

        if(hasText(String.valueOf(request.getCategory()))){
            existingProduct.setCategory(request.getCategory());
        }

        if(hasText(request.getDescription())){
            existingProduct.setDescription(request.getDescription());
        }

        if(hasText(request.getImageUrl())){
            existingProduct.setImageUrl(request.getImageUrl());
        }

        Product savedProduct =productRepository.save(existingProduct);
        return toProductResponse(savedProduct);
    }

    public Boolean deleteProduct(Long id){
        Product product = productRepository.findById(id).orElse(null);
        if(product == null){
            return false;
        }
        productRepository.delete(product);
        return true;
    }

}

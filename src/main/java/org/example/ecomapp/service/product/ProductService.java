package org.example.ecomapp.service.product;

import org.example.ecomapp.dto.product.ProductRequest;
import org.example.ecomapp.dto.product.ProductResponse;
import org.example.ecomapp.exception.CustomException;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.util.FieldUtils;
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
        response.setActive(product.getActive());
        return response;
    }
//    public ProductResponse fetchProduct(Long id){
//        return productRepository.findByIdAndActiveTrue(id)
//                .map(this::toProductResponse)
//                .orElse(null);
//    }

    public ProductResponse fetchProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            throw new CustomException(
                    "Product with ID " + id + " was not found."
            );
        }

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new CustomException(
                    "Product '" + product.getName()
                            + "' is currently unavailable."
            );
        }

        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            throw new CustomException(
                     product.getName() + " , is out of stock."
            );
        }

        return toProductResponse(product);
    }

    public List<ProductResponse> fetchProducts(){
        return productRepository.findByActiveTrue()
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
        if(FieldUtils.hasText(request,ProductRequest::getName)){
            existingProduct.setName(request.getName());
        }

        if(FieldUtils.hasValue(request,ProductRequest::getPrice)){
            existingProduct.setPrice(request.getPrice());
        }

        if(FieldUtils.hasValue(request,ProductRequest::getQuantity)){
            existingProduct.setQuantity(request.getQuantity());
        }

        if(FieldUtils.hasValue(request,ProductRequest::getCategory)){
            existingProduct.setCategory(request.getCategory());
        }

        if(FieldUtils.hasText(request,ProductRequest::getDescription)){
            existingProduct.setDescription(request.getDescription());
        }

        if(FieldUtils.hasText(request,ProductRequest::getImageUrl)){
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

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }
}

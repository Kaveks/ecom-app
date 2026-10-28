package org.example.ecomapp.repository;

import org.example.ecomapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    @Query("""
            SELECT p
            FROM products p
            JOIN Inventory i ON i.product = p
            WHERE p.active = true
              AND (i.quantityOnHand - i.quantityReserved) > 0
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Product> searchProducts(@Param("keyword") String keyword);
}

package org.example.ecomapp.repository;

import org.example.ecomapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser_Id(Long userId);

    Optional<CartItem> findByIdAndUser_Id(
            Long cartItemId,
            Long userId
    );

    Optional<CartItem> findByUser_IdAndProduct_Id(
            Long userId,
            Long productId
    );
    void deleteByUser_Id(Long userId);
}

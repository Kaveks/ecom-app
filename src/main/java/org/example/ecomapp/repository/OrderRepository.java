package org.example.ecomapp.repository;

import org.example.ecomapp.enums.OrderStatus;
import org.example.ecomapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_Id(Long userId);
    // Add this for automatic reservation expiry
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);
}

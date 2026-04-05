package com.pehlione.kafka.repository;

import com.pehlione.kafka.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByUserUsernameOrderByCreatedAtDesc(String username);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUserUsername(Long id, String username);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByCreatedAtDesc();

    long countByCustomerEmailIgnoreCase(String customerEmail);

    long countByCustomerEmailIgnoreCaseAndStatus(String customerEmail, String status);
}

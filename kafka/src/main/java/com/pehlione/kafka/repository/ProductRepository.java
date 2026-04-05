package com.pehlione.kafka.repository;

import com.pehlione.kafka.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByNameAsc();
    List<Product> findAllByCategoryIdOrderByNameAsc(Long categoryId);
    long countByCategoryId(Long categoryId);
}

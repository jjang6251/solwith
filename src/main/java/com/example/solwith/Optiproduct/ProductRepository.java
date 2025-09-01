package com.example.solwith.Optiproduct;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 행 배타 락: MySQL -> SELECT ... FOR UPDATE

}

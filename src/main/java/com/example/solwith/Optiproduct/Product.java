package com.example.solwith.Optiproduct;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable = false)
    private int stock;

    /** 낙관적 락을 위한 버전 컬럼 (자동 증분) */
    @Version
    private Long version;

    public void decrease(int qty) {
        if(qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        if(stock < qty) throw new IllegalStateException("insufficient stock");
        this.stock -= qty;
    }

}

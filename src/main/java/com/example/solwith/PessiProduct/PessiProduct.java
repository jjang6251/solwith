package com.example.solwith.PessiProduct;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pessi_product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PessiProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int stock;

    // 선택: 있어도 되고 없어도 됨(낙관적 락 겸용)
    @Version
    private Long version;

    public void decrease(int qty) {
        if(qty <= 0) throw new IllegalArgumentException("qty > 0");
        if(stock < qty) throw new IllegalStateException("insufficient");
        this.stock -= qty;
    }

}

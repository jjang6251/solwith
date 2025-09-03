package com.example.solwith.deadlockimpl;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Item {
    @Id
    private Long id;
    private int qty;

    public Item(Long id, int qty) {
        this.id = id;
        this.qty = qty;
    }
}

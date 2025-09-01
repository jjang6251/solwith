package com.example.solwith.Optiproduct;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCommand {
    private final ProductRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseOnce(Long id, int qty) {
        var p = repo.findById(id).orElseThrow();
        p.decrease(qty); // 재고 차감 (엔티티에 @Version 필드 반드시 존재)
    }
}

package com.example.solwith.Optiproduct;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductCommand command;

    public void decreaseWithRetry(Long id, int qty) {
        int max = 5;
        long backoff = 50;
        for (int i = 1; i <= max; i++) {
            try {
                command.decreaseOnce(id, qty); // ✅ 프록시 경유 → REQUIRES_NEW 적용
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (i == max) throw e;
                try { Thread.sleep(backoff); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); throw new RuntimeException(ie);
                }
                backoff = Math.min(500, backoff * 2);
            }
        }
    }
}

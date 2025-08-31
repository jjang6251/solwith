package com.example.solwith.product;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    /** 1) 단발 시도: 충돌 시 예외를 바로 상위로 던짐 */
    @Transactional
    public void decreaseOnce(Long productId, int qty) {
        Product p = repo.findById(productId).orElseThrow();
        // (의도적으로 경합을 크게 하고 싶으면 Tread.sleep(100) 정도 삽입해도 괜찮다
        p.decrease(qty);
    }

    /** 2) 재시도 정책 포함: 충돌 시 정해진 횟수만큼 자동 재시도 */
    @Transactional
    public void decreaseWithRetry(Long productId, int qty) {
        int maxAttempts = 5;
        long backoffMs = 30; //간단한 점증 backoff
        for (int attempt = 1; ; attempt++) {
            try {
                Product p = repo.findById(productId).orElseThrow();
                p.decrease(qty);
                //flush는 트랜잭션 종료 시점에 일어난다. (명시하고 싶으면 repo.flush() 가능)
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt >= maxAttempts) throw e;
                try {
                    Thread.sleep(backoffMs * attempt);
                } catch (InterruptedException ignored) {
                }
                // **중요**: 같은 트랜잭션에서 재시도하면 영속성 컨텍스트가 충돌 상태일 수 있으므로
                // 보통은 트랜잭션 경계를 바깥으로 빼서 (REQUIRES_NEW) 재호출하는 패턴을 많이 사용한다.
            }
        }

    }
    /** 실무에서는 재시도 로직을 트랜잭션 밖에 두고, 내부는 @Transactional(REQUIRES_NEW)로 새 트랜잭션을 열어 호출하는 방식을 권장한다(영속성 컨택스트 오염방지) */
}

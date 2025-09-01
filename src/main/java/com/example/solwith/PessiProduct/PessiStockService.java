package com.example.solwith.PessiProduct;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessiStockService {
    private final PessiProductRepository repo;

    /** 기본: 블로킹. 먼저 잡은 트랜잭션이 끝나면 다음이 이어서 실행 */
    @Transactional
    public void decreaseWithPessimistic(Long id, int qty) {
        PessiProduct p = repo.findByIdForUpdate(id).orElseThrow();
        p.decrease(qty);
        // commit 시 실제 UPDATE. 먼저 잠궜으므로 Lost Update는 없다.
    }

    /** 실패-바로(NOWAIT) 패턴: 잠겨 있으면 즉시 예외 -> 상위에서 재시도 및 우회 처리 */
    @Transactional
    public void decreaseNoWait(Long id, int qty) {
        PessiProduct p = repo.findByIdForUpdateNowait(id).orElseThrow();
        p.decrease(qty);
    }
}

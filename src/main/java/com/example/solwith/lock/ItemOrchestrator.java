package com.example.solwith.lock;

import com.example.solwith.common.DeadlockRetryExecutor;
import com.example.solwith.deadlockimpl.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemOrchestrator {
    private final ItemService itemService; // 내부 @Transactional 비즈니스 로직
    private final DeadlockRetryExecutor retry; // 재시도 유틸

    public void doWithRetry_AthanB(Long a, Long b) {
        retry.execute(5, 50, () -> itemService.lockOrderAthanB(a, b));
    }

    public void doWithRetry_BthenA(Long a, Long b) {
        retry.execute(5, 50, () -> itemService.lockOrderBthenA(a, b));
    }

    // 비즈니스 메서드(내부 @Transactional)에서 교착이 나도 상위에서 재시도 정책으로 감싸 회복한다.
}

package com.example.solwith.common;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Deadlock/락 경합 등 "일시적 동시성 실패"를 REQUIRES_NEW 트랜잭션 단위로 재시도한다.
 * - Spring 6.x: DeadlockLoserDataAccessException deprec → ConcurrencyFailureException 권장
 * - 필요시 PessimisticLockingFailureException 등 구체 타입을 함께 캐치
 */
@Component
@RequiredArgsConstructor
public class DeadlockRetryExecutor {

    private final PlatformTransactionManager txm;

    public <T> T execute(int maxAttempts, long baseBackoffMs, Supplier<T> work) {
        TransactionTemplate tt = new TransactionTemplate(txm);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        ConcurrencyFailureException last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tt.execute(status -> work.get());
            } catch (ConcurrencyFailureException e) {
                last = (e instanceof ConcurrencyFailureException)
                        ? (ConcurrencyFailureException) e
                        : new ConcurrencyFailureException(e.getMessage(), e);

                if (attempt == maxAttempts) break;

                // 지수 백오프 + 지터 (상한 1s 예시)
                long backoff = (long) (baseBackoffMs * Math.pow(2, attempt - 1));
                long jitter = ThreadLocalRandom.current().nextLong(0, baseBackoffMs);
                long sleepMs = Math.min(1_000L, backoff + jitter);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        throw last;
    }

    public void execute(int maxAttempts, long baseBackoffMs, Runnable work) {
        execute(maxAttempts, baseBackoffMs, () -> { work.run(); return null; });
    }
}
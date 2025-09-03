package com.example.solwith.deadlockimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository repo;

    @Transactional
    public void lockOrderAthanB(Long a, Long b) {
        repo.lockById(a);
        sleep(200); //의도적인 인터리빙
        repo.lockById(b); // 여기서 교착 가능
    }

    @Transactional
    public void lockOrderBthenA(Long a, Long b) {
        repo.lockById(b);
        sleep(200);
        repo.lockById(a);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms);} catch (InterruptedException ignored) {}
    }
}

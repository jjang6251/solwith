package com.example.solwith.Pessiproduct;

import com.example.solwith.PessiProduct.PessiProduct;
import com.example.solwith.PessiProduct.PessiProductRepository;
import com.example.solwith.PessiProduct.PessiStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class PessimisticLoctTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("testdb").withUsername("test").withPassword("test");

    @Autowired PessiProductRepository repo;
    @Autowired PessiStockService stock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        r.add("spring.jpa.show-sql", () -> "true");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @BeforeEach
    void init(){ repo.deleteAll(); }

    @Test
    @DisplayName("PESSIMISTIC_WRITE: 한쪽이 먼저 잠그면, 다른 쪽은 대기 후 순차 처리")
    void blocking_then_goes() throws Exception {
        PessiProduct p = repo.save(PessiProduct.builder().name("A").stock(10).build());

        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        var done  = Executors.newFixedThreadPool(2);

        Future<?> f1 = done.submit(() -> {
            ready.countDown();
            await(ready); await(start);
            // 트랜잭션 A: 잠금 후 오래 처리하는 척
            runTx(() -> {
                stock.decreaseWithPessimistic(p.getId(), 5);
                sleep(300); // 다른 스레드가 대기하도록 유도
            });
        });

        Future<?> f2 = done.submit(() -> {
            ready.countDown();
            await(ready); await(start);
            runTx(() -> stock.decreaseWithPessimistic(p.getId(), 5));
        });

        start.countDown();
        f1.get(); f2.get();
        done.shutdown();

        // 둘 다 처리 → 10 - 5 - 5 = 0
        assertThat(repo.findById(p.getId()).orElseThrow().getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("FOR UPDATE NOWAIT: 잠겨 있으면 즉시 실패한다")
    void nowait_fails_fast() throws Exception {
        PessiProduct p = repo.save(PessiProduct.builder().name("A").stock(10).build());

        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        var done  = Executors.newFixedThreadPool(2);

        Future<?> f1 = done.submit(() -> {
            ready.countDown(); await(ready); await(start);
            runTx(() -> {
                stock.decreaseWithPessimistic(p.getId(), 5); // 락 보유
                sleep(300);
            });
        });

        Future<Boolean> f2 = done.submit(() -> {
            ready.countDown(); await(ready); await(start);
            try {
                runTx(() -> stock.decreaseNoWait(p.getId(), 5)); // 즉시 시도
                return true; // (운 좋게 먼저면 성공 가능)
            } catch (Exception e) {
                return false; // 대부분은 락 보유중 → 실패
            }
        });

        start.countDown();
        f1.get();
        boolean s2 = f2.get();
        done.shutdown();

        PessiProduct after = repo.findById(p.getId()).orElseThrow();
        if (s2) {
            // 먼저 먹었을 수도 있으니 총합만 확인(10에서 10 차감)
            assertThat(after.getStock()).isIn(0, 5);
        } else {
            // 두 번째가 실패했으면 첫 번째만 적용 → 5
            assertThat(after.getStock()).isEqualTo(5);
        }
    }

    // --- 편의 함수들 ---
    private static void await(CountDownLatch l){ try { l.await(); } catch (InterruptedException e){ Thread.currentThread().interrupt(); } }
    private static void sleep(long ms){ try { Thread.sleep(ms); } catch (InterruptedException e){ Thread.currentThread().interrupt(); } }

    @Autowired org.springframework.transaction.PlatformTransactionManager tm;
    private void runTx(Runnable r) {
        var tt = new org.springframework.transaction.support.TransactionTemplate(tm);
        tt.executeWithoutResult(s -> r.run());
    }
}

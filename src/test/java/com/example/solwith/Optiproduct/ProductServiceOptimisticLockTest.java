package com.example.solwith.Optiproduct;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers // ✅ JUnit5 확장과 연계하여 Testcontainers 라이프사이클을 관리
@SpringBootTest // ✅ 스프링 컨텍스트를 띄워 실제 빈(Repo/Service) 주입 → 통합 테스트
@ActiveProfiles("test") // ✅ 테스트 프로파일 사용(필요 시 분리된 설정 적용)
public class ProductServiceOptimisticLockTest {

    /**
     * ✅ Testcontainers: MySQL 8.4.0 컨테이너를 테스트 동안만 띄워 사용
     *  - 정적 필드 + @Container: 클래스 단위로 한 번 띄우고 재사용 (성능)
     *  - 실제 DB를 사용하므로 JPA/Hibernate의 낙관적 락 동작을 현실적으로 검증 가능
     */
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired ProductRepository repo; // ✅ 실제 스프링 빈 주입
    @Autowired ProductService service; // ✅ 실제 서비스(낙관적 락 로직 포함) 주입
    @Autowired ProductCommand commandService;

    /**
     * ✅ 스프링 데이터소스 설정을 Testcontainers에서 띄운 MySQL로 바인딩
     *  - url 은 getJdbcUrl() 이어야 함(오타 주의)
     *  - driver-class-name 은 생략해도 되는 경우가 많지만 명시적 지정 가능
     */
    @DynamicPropertySource
    static void mysqlProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl); // 🔧 fix: getUsername → getJdbcUrl
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    /**
     * ✅ 각 테스트 시작 전 깨끗한 상태 보장
     *  - 재고/버전 초기화를 위해 테이블 비우기
     */
    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("동시에 5씩 차감: 단발 시도(decreaseOnce) -> 한쪽 성공, 한쪽은 Optimistic Lock 예외")
    void optimisticLock_conflict_happens() throws Exception {
        // given: 초기 재고 10
        Product p = repo.save(Product.builder().name("A").stock(10).build());

        int threads = 2; // 두 스레드가 동시에 동일 상품 재고를 5씩 차감 시도 → 충돌 유도
        ExecutorService es = Executors.newFixedThreadPool(threads);

        // CountDownLatch 두 개로 '동시 시작' 제어:
        // - ready: 모든 스레드가 준비되었는지 확인
        // - start: 준비 끝나면 동시에 출발(=락 경합 상황을 최대화)
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();

        for(int i = 0; i < threads; i++) {
            results.add(es.submit(() -> {
                ready.countDown(); // 준비 완료 알림
                start.await();     // 출발 신호 대기
                try {
                    // 단발 시도 메서드: 실패 시 재시도 없이 예외(OptimisticLockException 등) 발생 기대
                    commandService.decreaseOnce(p.getId(), 5);
                    return true;  // 성공
                } catch (Exception e) {
                    return false; // 실패(낙관적 락 충돌)
                }
            }));
        }

        ready.await();      // 두 스레드 모두 준비될 때까지 대기
        start.countDown();  // 동시에 출발!
        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);

        // 결과 수집: true(성공) 개수 카운트
        long success = results.stream().filter(f -> {
            try {
                return f.get(); // true=성공, false=실패
            } catch (Exception e) {
                return false;
            }
        }).count();

        // then
        // 🔧 기대값: 2명 중 1명만 성공해야 함(동시에 같은 레코드를 갱신 → 한쪽은 버전 불일치로 충돌)
        assertThat(success).isEqualTo(1); // 🔧 fix: 기존 5 → 1

        // 실제 재고는 5가 되어야 함(10에서 한 번만 5 차감 성공)
        Product after = repo.findById(p.getId()).orElseThrow();
        assertThat(after.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("동시에 5씩 차감: 재시도(decreaseWithRetry) → 둘 다 성공, 총 10 차감")
    void optimisticLock_retry_both_success() throws Exception {
        // given: 초기 재고 10
        Product p = repo.save(Product.builder().name("A").stock(10).build());

        int threads = 2;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            results.add(es.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    // 재시도 로직 포함: 충돌(OptimisticLock) 발생 시 일정 횟수/백오프 전략으로 재시도 → 결국 양쪽 모두 성공 기대
                    service.decreaseWithRetry(p.getId(), 5);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }));
        }

        ready.await();
        start.countDown();
        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);

        // 두 스레드 모두 성공했는지 확인
        long success = results.stream().filter(f -> {
            try { return f.get(); } catch (Exception e) { return false; }
        }).count();

        // then
        // 재시도 덕분에 둘 다 성공해야 함
        assertThat(success).isEqualTo(2);

        // 총 10 차감 → 재고 0
        Product after = repo.findById(p.getId()).orElseThrow();
        assertThat(after.getStock()).isEqualTo(0);
    }
}
# 📖 Spring Boot 학습 정리 (B단계 ~ C단계)

## B단계: 스프링 핵심 원리

### 1. 스프링 컨테이너와 빈 등록
스프링에서는 객체를 직접 생성하지 않고 `@Configuration` 클래스와 `@Bean` 메서드를 통해 **스프링 컨테이너**가 객체를 생성하고 관리합니다.  
이렇게 등록된 객체를 **스프링 빈(Bean)** 이라고 부릅니다.

- 장점: 객체 생명주기를 컨테이너가 관리 → 메모리 효율, 재사용성 증가
- 코드 예시:
```java
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl();
    }
}
```

### 2. 싱글톤 패턴 vs 스프링 싱글톤
- **직접 구현한 싱글톤 패턴**  
  → 인스턴스를 하나만 만들도록 static 필드로 관리. 코드가 복잡하고 테스트 어려움.

- **스프링 싱글톤 컨테이너**  
  → 기본 스코프가 싱글톤이므로, 같은 빈을 여러 번 주입받아도 실제 객체는 1개.  
  개발자가 직접 싱글톤 패턴을 구현할 필요가 없음.

### 3. 의존관계 주입 (DI)
스프링이 객체 간 의존관계를 자동으로 연결해줌.  
**생성자 주입**이 가장 권장됨.

```java
@Service
public class MemberService {
    private final MemberRepository repository;

    @Autowired
    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }
}
```

- 장점: 불변성 보장, 테스트 용이성 증가, 순환참조 방지

### 4. 스프링 MVC 요청 흐름
- `DispatcherServlet`이 모든 요청을 받아서 컨트롤러에 위임
- 컨트롤러 → 서비스 → 리포지토리 계층 순으로 실행
- 응답은 뷰 리졸버(ViewResolver) 또는 JSON 변환을 통해 클라이언트로 전달

### 5. HTTP 요청 데이터 처리
- `@RequestParam` : 단일 파라미터 매핑
- `@ModelAttribute` : 객체 바인딩
- `@RequestBody` : JSON 요청 매핑

### 6. HTTP 응답 처리
- `@ResponseBody` : 객체 → JSON 변환
- `@RestController` : `@Controller + @ResponseBody` 조합 → REST API 응답에 적합

---

## C단계: 기능 확장 & 고도화

### 1. JSON 응답 처리
스프링은 내부적으로 `HttpMessageConverter`(Jackson)를 사용하여 객체를 JSON으로 자동 변환합니다.

- 코드 예시:
```java
@RestController
public class MemberController {
    @GetMapping("/api/members")
    public List<Member> findAll() {
        return memberService.findAll();
    }
}
```

### 2. DTO와 응답 포맷 개선
엔티티를 직접 노출하지 않고 **DTO(Data Transfer Object)** 로 응답을 전달.  
추가로 공통 응답 포맷(`ApiResponse<T>`)을 정의하여 일관된 API 설계.

```json
{
  "status": 200,
  "message": "OK",
  "data": { ... },
  "traceId": "uuid",
  "timestamp": "2025-08-18T19:00:00"
}
```

### 3. Validation 적용
요청 DTO에 제약 조건을 추가해 유효성을 검증.

```java
public class MemberRequest {
    @NotBlank
    private String name;
}
```

- `@Valid`와 함께 사용 → 잘못된 요청 시 `MethodArgumentNotValidException` 발생
- 전역 예외 처리기로 잡아서 `ApiResponse` 포맷으로 반환

### 4. 제네릭 충돌 문제 (Void vs Object)
`ApiResponse.error()` 호출 시 제네릭 타입이 `Object`로 추론되는 문제 발생.  
해결 방법:
```java
ApiResponse<Void> body = ApiResponse.error(...);
return ResponseEntity.badRequest().body(body);
```

### 5. stream()의 역할
자바 스트림 API는 컬렉션 데이터를 선언적으로 처리하기 위한 기능.

```java
return memberService.findMembers().stream()
        .map(m -> new MemberResponse(m.getId(), m.getName()))
        .toList();
```

- 장점: 코드 간결성, 병렬 처리 지원

### 6. ResponseEntity의 역할
HTTP 응답을 세밀하게 제어 가능.

```java
return ResponseEntity
        .status(HttpStatus.CREATED)
        .header("X-Custom", "value")
        .body(responseDto);
```

- 상태 코드, 헤더, 바디를 자유롭게 설정 가능

---

## 운영 품질 개선

### 1. TraceId
- 요청마다 UUID를 생성하여 응답 JSON + 헤더(`X-Trace-Id`)에 추가
- 로그와 클라이언트를 연결해 장애 추적이 쉬움
- 마이크로서비스 환경에서는 분산 추적 필수 요소

### 2. RequestLoggingFilter
- 요청/응답 실행 시간, 상태코드, 바디 크기 등을 기록
- 슬로우 요청(SLOW) 감지 가능
- `FilterRegistrationBean`으로 순서 제어하여 traceId와 함께 동작

### 3. Filter를 Bean으로 등록한 이유
- 실행 순서 보장 (`order` 값)
- 운영 환경/테스트 환경에 따라 등록 유연성
- `@Component`보다 명시적으로 제어 가능

### 4. Filter vs Interceptor vs AOP
- **Filter**: HTTP 레벨 공통 기능 (traceId, 로깅, 인증)
- **Interceptor**: 컨트롤러 전후 (인증/인가)
- **AOP**: 서비스 계층 공통 관심사 (트랜잭션, 성능 모니터링)

---

## 추가 학습 질문 정리

- **traceId를 왜 사용하는가?**  
  → 요청 단위 추적, 장애 분석, 마이크로서비스 간 요청 흐름 추적

- **common 패키지 + WebConfig 등록 이유?**  
  → 전역 공통 기능 제공, 필터 실행 순서 보장, 운영환경별 관리 용이

- **ApiResponse.ErrorDetail 오류**  
  → 내부 클래스라면 `static` 선언 필요 (직렬화 오류 방지)

---

# 📖 Spring Boot 학습 정리 (B단계 ~ C단계)

<details>
<summary>B단계: 스프링 핵심 원리</summary>
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
</details>

<details>
<summary>JWT_Auth_Flow</summary>

# JWT 인증 흐름 정리 (JwtAuthFilter · JwtProvider · SecurityConfig)

---

## 1) 요약

- **JwtProvider**: JWT **발급/검증** 유틸. 시크릿 키로 서명/검증, 클레임 추출.
- **JwtAuthFilter**: HTTP 요청에서 `Authorization: Bearer <JWT>` **파싱 → 검증 → SecurityContext 주입**.
- **SecurityConfig**: 시큐리티 **정책(인가 규칙, 세션/CSRF, 필터 순서)**을 정의.

---

## 2) 요청 1건의 처리 순서 (큰 그림)

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant F1 as TraceIdFilter
    participant F2 as JwtAuthFilter
    participant SC as Spring Security (Authorization)
    participant MVC as Controller

    C->>F1: HTTP Request
    F1-->>C: (set X-Trace-Id in header, MDC)
    F1->>F2: continue filter chain

    alt Authorization header with Bearer token
        F2->>F2: parse & verify with JwtProvider
        F2->>SC: set Authentication in SecurityContext
    else no / invalid token
        F2->>C: 401 (policy ①) OR throw exception (policy ②)
        Note over F2,C: 실패 정책에 따라 응답 혹은 전역 예외 처리로 위임
    end

    F2->>SC: continue
    SC->>SC: URL/메서드 권한 평가 (permitAll/authenticated/hasRole...)
    SC->>MVC: pass if authorized
    MVC-->>C: Response (includes X-Trace-Id, unified body)
```

> 권장 실패 정책: **필터에서 예외를 던져 전역 예외 처리기**가 공통 응답 포맷으로 변환하도록 구성(일관성 확보).

---

## 3) 구성요소별 역할 & 책임

### 3.1 JwtProvider — JWT 발급/검증 유틸리티

| 기능 | 설명 | 비고 |
|---|---|---|
| **createToken(subject, role, claims)** | `sub`, `role`, `iat`, `exp` 세팅 후 **서명**하여 문자열 토큰 발급 | JJWT 사용 (HMAC-SHA) |
| **parse(token)** | 시크릿 키로 **서명 검증**, 만료/위조/형식 오류 시 예외 | `Jws<Claims>` 반환 |
| **키 관리** | `application.yml/properties`의 `jwt.secret.key`로 Key 생성 | **최소 32바이트** 이상 권장 |
| **만료 설정** | `jwt.access-token-validity-seconds`로 토큰 만료 제어 | 운영 환경에서 짧게(예: 1h) |

**샘플 설정 (properties)**
```properties
jwt.secret.key=ThisIsADevOnlySecretKeyThatIsAtLeast32BytesLong!!!
jwt.access-token-validity-seconds=3600
```

---

### 3.2 JwtAuthFilter — 요청당 한 번 실행되는 인증 필터

| 단계 | 동작 | 결과 |
|---|---|---|
| 1 | `Authorization` 헤더 확인 (`Bearer <JWT>`) | 토큰 유무 판단 |
| 2 | `JwtProvider.parse()`로 검증 | 서명/만료/형식 검사 |
| 3 | 성공 시 `UsernamePasswordAuthenticationToken` 생성 | `SecurityContextHolder`에 **인증 객체 저장** |
| 4 | 실패 정책 | ① **즉시 401 응답** 또는 ② **예외 throw → 전역 핸들러 처리** |
| 5 | (선택) `MDC.put("user", username)` | 로그에 사용자 식별자 자동 포함 |

> 필터는 반드시 **`UsernamePasswordAuthenticationFilter` 앞**에 등록하여, 인가 단계 전에 인증을 완료하도록 한다.

---

### 3.3 SecurityConfig — 시큐리티 전반 정책

| 설정 | 내용 | 목적 |
|---|---|---|
| **Session = STATELESS** | 세션 비활성(Stateless) | JWT 형태에 적합 |
| **CSRF 비활성** | `csrf().disable()` | REST API 기본 |
| **인가 규칙** | `authorizeHttpRequests`로 **경로별 접근 정책** | 공개/보호 API 구분 |
| **필터 순서** | `addFilterBefore(new JwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` | 표준 인증 전 JWT 인증 수행 |
| **CORS/예외 엔트리포인트** | 필요 시 추가 | 클라이언트/보안 정책 정교화 |

---

## 4) 케이스별 동작 요약

- **공개 API** (`permitAll`)
  - 토큰 없음: 그대로 통과 → 컨트롤러 실행
  - 토큰 있음: 검증 성공 시 인증된 사용자로 접근(컨트롤러에서 `Authentication` 활용 가능)

- **보호 API** (`authenticated`/`hasRole`)
  - 토큰 없음/무효: 인가 단계에서 401/403
  - 유효 토큰: 인증 객체 세팅 → 인가 통과 → 컨트롤러 실행

---

## 5) 운영 팁 & 체크리스트

- [ ] `jwt.secret.key`는 **32바이트 이상**(HMAC-SHA256) — 짧으면 `WeakKeyException` 유발
- [ ] 실패 정책을 **전역 예외 처리기**로 통일 → `ApiResponse` 포맷 유지
- [ ] `TraceIdFilter`를 **가장 먼저** 실행해 로그/응답에 traceId 포함
- [ ] 로깅에 **MDC(traceId, user)**를 써서 장애 추적 용이성 확보
- [ ] 보호/공개 경로의 **패턴 매칭**이 겹치지 않는지 확인
- [ ] 토큰/민감정보는 **로그 마스킹** 적용

---

## 6) 미니 예시 (요약 형태)

```java
// SecurityConfig (요약)
http.csrf(csrf -> csrf.disable())
    .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login", "/health").permitAll()
        .requestMatchers("/api/members/**").authenticated()
        .anyRequest().permitAll())
    .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
```

```java
// JwtAuthFilter (요약)
protected void doFilterInternal(req, res, chain) {
  String header = req.getHeader("Authorization");
  if (hasBearer(header)) {
    var jws = jwtProvider.parse(token(header));
    var auth = new UsernamePasswordAuthenticationToken(jws.getPayload().getSubject(), null,
        List.of(new SimpleGrantedAuthority("ROLE_" + jws.getPayload().get("role", String.class))));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
  chain.doFilter(req, res);
}
```

```java
// JwtProvider (요약)
public String createToken(String username, String role) {
  Instant now = Instant.now();
  return Jwts.builder()
    .subject(username)
    .claim("role", role)
    .issuedAt(Date.from(now))
    .expiration(Date.from(now.plusSeconds(validity)))
    .signWith(key)
    .compact();
}
```

---

### 참고
- 토큰 실패를 **필터에서 직접 401로 쓰지 않고**, 커스텀 예외를 던져 전역 예외 처리기에서 공통 포맷으로 내려주는 방식이 더 낫다.
- 분산 추적을 계획한다면, `X-Trace-Id`와 **표준 trace 헤더**(W3C traceparent)를 병행 가능하다.

</details>




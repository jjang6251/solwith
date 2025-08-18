
# 스프링 부트 테스트 케이스 작성 가이드

## 1) 왜 테스트를 하나요?
- **회귀 방지**: 기능 수정해도 기존 동작이 망가지지 않게.
- **설계 개선**: 테스트 가능한 코드는 보통 결합도가 낮고 역할이 분리되어 있음.
- **문서 역할**: 테스트는 **코드로 된 사용 예시**다.
- **속도**: QA/수동 테스트 시간을 줄이고 배포 속도를 올린다.

---

## 2) 테스트 피라미드
- **단위(Unit) 테스트**: 가장 많고, 가장 빠르다.
- **슬라이스(Slice) 테스트**: 웹 레이어만, JPA 레이어만 등 일부만 로딩.
- **통합(Integration) 테스트**: 스프링 컨텍스트 전체.
- **E2E**: 외부 시스템 포함.

---

## 3) 기본 스택
- **JUnit 5**
- **AssertJ**
- **Mockito**

---

## 4) 테스트 구조 (AAA/GWT)
- **Arrange(Given)**: 준비
- **Act(When)**: 실행
- **Assert(Then)**: 검증

---

## 5) 테스트 레벨

### 5.1 단위 테스트
- 비즈니스 로직만 검증.
```java
@Test
void findOne_notFound_throws() {
    given(repo.findById(999L)).willReturn(Optional.empty());
    assertThatThrownBy(() -> sut.findOne(999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("999");
}
```

### 5.2 슬라이스 테스트
- HTTP 레이어, JSON 포맷 검증.
```java
@Test
void get_notFound() throws Exception {
    given(service.findOne(999L)).willThrow(new NotFoundException("Member not found: 999"));
    mvc.perform(get("/api/members/999"))
       .andExpect(status().isNotFound())
       .andExpect(jsonPath("$.status").value(404));
}
```

### 5.3 통합 테스트
- 필터/빈/설정 포함.
```java
@Test
void traceId_header_exists() throws Exception {
    mvc.perform(get("/api/members/health"))
       .andExpect(status().isOk())
       .andExpect(header().exists("X-Trace-Id"));
}
```

---

## 6) 테스트 데이터 전략
- 빌더 패턴, Mother Object, 중복 제거, 독립성 보장.

---

## 7) 검증 대상 (이 프로젝트 기준)
1. 공통 응답 포맷 (status/message/data/errors/...)
2. 유효성 실패 → errors 배열 확인
3. 예외 흐름 404/500 구분
4. 필터 TraceId 헤더 확인
5. 경계/부정 케이스

---

## 8) 테스트 계획 체크리스트

### 서비스
- join 저장
- findOne 없는 ID → 예외
- update 이름 변경
- delete 정상 삭제

### 컨트롤러
- GET 200/404
- POST 201/400
- PUT 200
- DELETE 204

### 검색/페이징
- 필터, 정렬, 페이지

### 필터/운영
- TraceId 헤더

---

## 9) 네이밍/스타일
- 한글 `@DisplayName`
- 메서드명: `getMember_whenExists_returns200()`
- 하나의 테스트 = 하나의 기대
- 테스트는 계약이다

---

## 10) CI 고려
- 컨텍스트 캐시 활용
- 슬라이스 중심
- 랜덤/시간 의존 제거
- 순서 의존 금지

---

## 11) 예시 3개

### 서비스 단위
```java
@Test
void findOne_notFound() {
    given(repo.findById(999L)).willReturn(Optional.empty());
    assertThatThrownBy(() -> sut.findOne(999L))
        .isInstanceOf(NotFoundException.class);
}
```

### 컨트롤러 슬라이스
```java
@Test
void create_validationFail() throws Exception {
    mvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
       .andExpect(status().isBadRequest())
       .andExpect(jsonPath("$.errors[0].field").value("name"));
}
```

### 통합 테스트
```java
@Test
void traceId_header_exists() throws Exception {
    mvc.perform(get("/api/members/health"))
       .andExpect(status().isOk())
       .andExpect(header().exists("X-Trace-Id"));
}
```

---

## 12) 안티패턴
- 모든 걸 통합 테스트로만 돌리기
- DB/외부 연동 몰래 사용
- 과도한 목킹
- 커버리지 집착

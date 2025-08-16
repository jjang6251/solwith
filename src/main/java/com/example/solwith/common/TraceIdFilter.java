package com.example.solwith.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/*
* TraceId의 목적
	1.	요청 단위 추적 (Request Tracing)
	•	서버로 들어온 하나의 HTTP 요청마다 고유한 ID를 부여.
	•	로그에 같은 traceId가 찍히면, “이 요청이 서비스 계층 → DB → 외부 API 호출까지 어떻게 흘렀는지” 한눈에 추적 가능.
	2.	장애 원인 분석
	•	클라이언트가 “이 요청 이상했어요”라고 할 때, 클라이언트가 응답 헤더/바디에 담긴 traceId를 알려주면 서버 로그에서 해당 요청만 빠르게 찾아낼 수 있음.
	3.	분산 환경/마이크로서비스에서의 요청 연결
	•	예: API Gateway → 회원 서비스 → 주문 서비스 → 결제 서비스 …
	•	각 마이크로서비스가 공통 traceId를 로그에 남기면, 여러 시스템에 퍼져 있는 로그를 이어서 “이 요청의 흐름”을 재구성 가능.
	•	실제로 Zipkin, Jaeger, Sleuth, OpenTelemetry 같은 도구들이 traceId 기반으로 동작.
	4.	보안/감사(Audit) 로그
	•	중요한 거래 API(예: 결제, 블록체인 전송)에서 traceId를 남기면 “이 사용자가 언제 어떤 요청을 했는가”를 증거성 있게 기록 가능.
* */
public class TraceIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID = "traceId";
    public static final String HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();

        MDC.put(TRACE_ID, traceId);

        try {
            response.setHeader(HEADER, traceId);
            request.setAttribute(TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}

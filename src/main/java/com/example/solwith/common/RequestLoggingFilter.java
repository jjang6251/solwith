package com.example.solwith.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final long SLOW_MS = 700;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        String traceId = (String) request.getAttribute(TraceIdFilter.TRACE_ID);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long took = System.currentTimeMillis() - start;
            int status = responseWrapper.getStatus();

            // 간단 헤더/쿼리 로 (민감한 정보는 마스킹)
            String q = request.getQueryString();
            String query = (q == null ? "" : "?" + q);
            String ua = mask(headerFirst(request, "User-Agent"));
            int reqSize = requestWrapper.getContentAsByteArray().length;
            int resSize = responseWrapper.getContentAsByteArray().length;

            String level = took >= SLOW_MS ? "[Time]SLOW" : "[Time]OK";

            log.info("[{}] {} {}{} -> {} ({} ms) traceId={} reqB={}B resB={}B ua={}",
                    level, method, uri, query, status, took, traceId, reqSize, resSize, ua);

            //래퍼의 내용을 실제 응답으로 복사
            responseWrapper.copyBodyToResponse();

        }
    }

    //헤더의 첫번째 값을 뽑아오는 함수
    private String headerFirst(HttpServletRequest req, String name) {
        Enumeration<String> e = req.getHeaders(name);
        return (e != null && e.hasMoreElements()) ? e.nextElement() : null;
    }

    //너무 긴 문자열을 잘라서 로그에 출력하기 위한 mask 함수
    private String mask(String v) {
        if(v == null) return null;
        return v.length() > 200 ? v.substring(0, 200) + "...(trunc)" : v;
    }
}

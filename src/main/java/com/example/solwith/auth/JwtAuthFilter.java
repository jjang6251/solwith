package com.example.solwith.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    public JwtAuthFilter(JwtProvider jwtProvider) {this.jwtProvider = jwtProvider;}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var jws = jwtProvider.parse(token);
                String username = jws.getPayload().getSubject();
                String role = jws.getPayload().get("role", String.class);
                List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, auths);

                // 선택 사항: traceId와 함께 사용자도 MDC에 넣기
                MDC.put("user", username);
            } catch (Exception e) {
                // 토큰이 있되 잘못된 경우 -> 401로 보낼지, 지나가게 할지 정책 결정
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("user");
            SecurityContextHolder.clearContext();
        }
    }
}

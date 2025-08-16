package com.example.solwith.config;

import com.example.solwith.common.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//로그 패턴에 %X{traceId}를 넣어두면 로그에서도 같은 traceId로 묶여 확인 가능 (logback 설정)
@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TraceIdFilter());
        reg.setOrder(1);
        return reg;
    }
}

package com.example.solwith.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.IntStream;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Set<String> SENSITIVE_KEYS = Set.of("password", "pwd", "secret", "token", "authorization");

    @Around("@annotation(com.example.solwith.aop.LogExecutionTime)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        // 파라미터 로깅(마스킹)
        String argsStr = buildArgs(sig.getMethod().getParameters(), pjp.getArgs());

        try {
            Object result = pjp.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("[AOP] {} took={}ms traceId={} args={} resultType={}",
                    method, took, MDC.get("traceId"), argsStr,
                    (result==null ? "void" : result.getClass().getSimpleName()));
            return result;
        } catch (Throwable t) {
            long took = System.currentTimeMillis() - start;
            log.warn("[AOP] {} EX took={}ms traceId={} args={} ex={}:{}",
                    method, took, MDC.get("traceId"), argsStr,
                    t.getClass().getSimpleName(), t.getMessage());
            throw t;
        }
    }

    private String buildArgs(Parameter[] params, Object[] values) {
        return IntStream.range(0, params.length)
                .mapToObj(i -> {
                    String name = params[i].getName();
                    Object v = values[i];
                    String printed = stringify(name, v);
                    return name + "=" + printed;
                })
                .reduce((a,b) -> a + ", " + b).orElse("-");
    }

    private String stringify(String name, Object v) {
        if (v == null) return "null";
        if (isSensitive(name)) return "***";
        // 간단 객체는 toString, 복잡 객체는 클래스명만
        if (v instanceof CharSequence || v instanceof Number || v instanceof Boolean) return v.toString();
        if (v.getClass().isArray()) return v.getClass().getComponentType().getSimpleName() + "[]";
        if (v instanceof Collection<?> c) return "Collection(size=" + c.size() + ")";
        if (v instanceof Map<?,?> m) return "Map(size=" + m.size() + ")";
        return v.getClass().getSimpleName();
    }

    private boolean isSensitive(String paramName) {
        String lower = paramName.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(lower::contains);
    }
}

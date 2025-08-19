package com.example.solwith.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger) 설정 클래스
 * - API 문서의 기본 메타데이터(제목/버전/설명) 정의
 * - 전역 보안 스키마(X-API-KEY 헤더 기반) 등록
 * - 특정 경로 그룹(/api/members/**)을 별도 그룹으로 노출
 */
@Configuration
public class OpenApiConfig {

    /**
     * Swagger/OpenAPI의 전역 스펙 빈.
     * - info(): UI 상단/명세에 보일 서비스 메타 정보
     * - addSecurityItem(): 전역 보안 요구사항(= 이 스키마를 쓰겠다고 선언)
     * - components().addSecuritySchemes(): 보안 스키마(이름=ApiKeyAuth) 정의
     *
     * ⚠️ 주의: 여기서 보안을 "문서에 선언"만 한다.
     *          실제로 X-API-KEY를 검사/인증하려면 Interceptor/Filter 등에서 검증 로직이 필요합니다.
     */
    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring CRUD API")
                        .version("v1")
                        .description("공통 응답 포맷 + TraceId + 간단 API Key 인증이 적용된 예"))
                // 이 문서의 모든 엔드포인트가 "ApiKeyAuth" 스키마를 사용한다고 선언(전역)
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
                // 보안 스키마 정의: Header에 'X-API-KEY'를 담는 API KEY 타입
                .components(new Components().addSecuritySchemes(
                        "ApiKeyAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)       // APIKEY / HTTP / OAUTH2 / OPENIDCONNECT 등
                                .in(SecurityScheme.In.HEADER)           // 헤더에 전달
                                .name("X-API-KEY")                      // 헤더 키 이름
                                .description("변경성 요청(POST/PUT/DELETE) 시 필수") // 설명(표시용)
                ));
    }

    /**
     * Swagger UI에서 엔드포인트를 그룹으로 묶어 보여주기 위한 설정(선택).
     * - group("members"): UI 좌측/상단에 'members' 그룹으로 나타남
     * - pathsToMatch("/api/members/**"): 이 경로들만 해당 그룹에 포함
     *
     * 필요 시 여러 그룹 빈을 추가해 도메인/버전별로 분리 가능합니다.
     */
    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("members")
                .pathsToMatch("/api/members/**")
                .build();
    }
}
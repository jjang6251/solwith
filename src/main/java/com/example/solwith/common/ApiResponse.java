package com.example.solwith.common;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//API 공통 응답 포맷 + 에러 메시지 개선
//공통 응답 포맷 클래스 만들기
@Schema(description = "공통 응답 포맷")
public class ApiResponse<T> {
    @Schema(description = "HTTP 상태 코드", example = "200/201/400/500")
    private final int status; //HTTP Status code
    @Schema(description = "메시지", example = "OK")
    private final String message; // OK 및 에러 메시지
    @Schema(description = "성공 데이터")
    private final T data; // 성공 데이터
    @Schema(description = "에러 상세 목록", nullable = true)
    private final List<ErrorDetail> errors; //에러 상세(성공시 null 반환)
    @Schema(description = "요청 경로", example = "/api/members/1")
    private final String path;             // 요청 URI
    @Schema(description = "추적 ID", example = "b6f4c4ae-7f...")
    private final String traceId;          // 요청 추적 ID (로그 상관관계)
    @Schema(description = "타임스탬프", example = "2025-08-19T12:34:56.789")
    private final LocalDateTime timestamp; // 응답 생성 시각
    @Schema(description = "메타 정보(페이징 등)", nullable = true)
    private final Map<String, Object> meta;// 추가 메타(페이지 정보 등)


    public ApiResponse(int status, String message, T data, List<ErrorDetail> errors, String path, String traceId, LocalDateTime timestamp, Map<String, Object> meta) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.path = path;
        this.traceId = traceId;
        this.timestamp = timestamp;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> success(T data, String path, String traceId) {
        return new ApiResponse<>(200, "OK", data, null, path, traceId, LocalDateTime.now(), null);
    }

    public static <T> ApiResponse<T> success(T data, String path, String traceId, Map<String, Object> meta) {
        return new ApiResponse<>(200, "OK", data, null, path, traceId, LocalDateTime.now(), meta);
    }

    public static <T> ApiResponse<T> error(int status, String message, String path, String traceId, List<ErrorDetail> errors) {
        return new ApiResponse<>(status, message, null, errors, path, traceId, LocalDateTime.now(), null);
    }

    // getters …
    public int getStatus(){ return status; }
    public String getMessage(){ return message; }
    public T getData(){ return data; }
    public List<ErrorDetail> getErrors(){ return errors; }
    public String getPath(){ return path; }
    public String getTraceId(){ return traceId; }
    public LocalDateTime getTimestamp(){ return timestamp; }
    public Map<String, Object> getMeta(){ return meta; }

    // 에러 항목
    @Schema(description = "에러 항목")
    public static class ErrorDetail {
        @Schema(description = "에러 필드명", example = "name", nullable = true)
        private final String field;     // 필드명(없으면 null)
        @Schema(description = "검증/에러 코드", example = "NotBlank", nullable = true)
        private final String reason;    // 사유 코드/키(선택)
        @Schema(description = "메시지", example = "name은 필수입니다.")
        private final String message;   // 사용자 메시지
        public ErrorDetail(String field, String reason, String message) {
            this.field = field; this.reason = reason; this.message = message;
        }
        public String getField(){ return field; }
        public String getReason(){ return reason; }
        public String getMessage(){ return message; }
    }
}

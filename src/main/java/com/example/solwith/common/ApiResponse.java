package com.example.solwith.common;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//API 공통 응답 포맷 + 에러 메시지 개선
//공통 응답 포맷 클래스 만들기
public class ApiResponse<T> {
    private final int status; //HTTP Status code
    private final String message; // OK 및 에러 메시지
    private final T data; // 성공 데이터
    private final List<ErrorDetail> errors; //에러 상세(성공시 null 반환)
    private final String path;             // 요청 URI
    private final String traceId;          // 요청 추적 ID (로그 상관관계)
    private final LocalDateTime timestamp; // 응답 생성 시각
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
    public static class ErrorDetail {
        private final String field;     // 필드명(없으면 null)
        private final String reason;    // 사유 코드/키(선택)
        private final String message;   // 사용자 메시지
        public ErrorDetail(String field, String reason, String message) {
            this.field = field; this.reason = reason; this.message = message;
        }
        public String getField(){ return field; }
        public String getReason(){ return reason; }
        public String getMessage(){ return message; }
    }
}

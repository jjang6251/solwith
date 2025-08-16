package com.example.solwith.common;

//API 공통 응답 포맷 + 에러 메시지 개선
//공통 응답 포맷 클래스 만들기
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final T data;


    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success (T data) {
        return new ApiResponse<>(200, "OK", data);
    }

    public static <T> ApiResponse<T> success (String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error (int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    public int getStatus() {return status;}
    public String getMessage() {return message;}
    public T getData() {return data;}
}

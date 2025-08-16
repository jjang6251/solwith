package com.example.solwith.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //에러 처리 개선 전
//    @ExceptionHandler(NotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e){
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
//    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
//        String msg = e.getBindingResult().getFieldErrors().stream()
//                .findFirst().map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
//                .orElse("Validation error");
//        return ResponseEntity.badRequest()
//                .body(new ErrorResponse(msg, HttpStatus.BAD_REQUEST.value()));
//    }

    //에러 처리 개선 후
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
    }
}

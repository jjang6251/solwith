package com.example.solwith.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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

    //에러 처리 개선 후 1
//    @ExceptionHandler(NotFoundException.class)
//    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(404, e.getMessage()));
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
//        String msg = e.getBindingResult().getFieldErrors().stream()
//                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
//                .findFirst().orElse("Validation error");
//        return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
//    }

    //에러 처리 개선 후 2
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e,
                                                              HttpServletRequest req){
        List<ApiResponse.ErrorDetail> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiResponse.ErrorDetail(
                        fe.getField(),        // field
                        fe.getCode(),         // reason(예: NotBlank, Size)
                        fe.getDefaultMessage()// message
                )).toList();

        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                req.getRequestURI(),
                traceId(req),
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e, HttpServletRequest req){
        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                req.getRequestURI(),
                traceId(req),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class) // 마지막 안전망
    public ResponseEntity<ApiResponse<Void>> handleOthers(Exception e, HttpServletRequest req){
        ApiResponse<Void> body = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error",
                req.getRequestURI(),
                traceId(req),
                List.of(new ApiResponse.ErrorDetail(null, e.getClass().getSimpleName(), e.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String traceId(HttpServletRequest req){
        Object v = req.getAttribute("traceId");
        return v != null ? v.toString() : null;
    }
}

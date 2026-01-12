package com.example.greenribboncalimassignment.web.advice;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비즈니스 로직 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("[Business Exception] Code: {}, Message: {}", e.getCode(), e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. @Valid 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("[Validation Failed] Target: {}", e.getBindingResult().getTarget());
        ErrorResponse response = ErrorResponse.of("INVALID_INPUT", "입력값이 올바르지 않습니다.", e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 3. 나머지 모든 예외 (알 수 없는 서버 에러)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Unhandled Exception]", e);
        ErrorResponse response = ErrorResponse.of("SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

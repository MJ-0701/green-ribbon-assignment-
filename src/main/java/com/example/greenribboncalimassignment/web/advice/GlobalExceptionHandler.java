package com.example.greenribboncalimassignment.web.advice;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.exception.ErrorResponse;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비즈니스 로직 예외 (BusinessException)
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ResultCode resultCode = e.getResultCode();

        log.warn("[Business Exception] Code: {}, Message: {}", resultCode.getCode(), resultCode.getMessage());

        // 수정 포인트: new 대신 static factory method 사용 (Record 필드 맞춤)
        ErrorResponse response = ErrorResponse.of(resultCode.getCode(), resultCode.getMessage());

        // HTTP Status 결정 로직
        HttpStatus httpStatus = HttpStatus.resolve(resultCode.getStatus());
        if (httpStatus == null) {
            httpStatus = (resultCode.getStatus() >= 5000) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(response, httpStatus);
    }

    // 2. @Valid 유효성 검사 실패 (RequestBody 필드 에러)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("[Validation Failed] Target: {}", e.getBindingResult().getTarget());

        // 수정 포인트: ErrorResponse.of 메서드로 BindingResult를 넘김 -> 상세 에러 생성
        ErrorResponse response = ErrorResponse.of(
                ResultCode.INVALID_INPUT_VALUE.getCode(),
                ResultCode.INVALID_INPUT_VALUE.getMessage(),
                e.getBindingResult()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 3. 그 외 모든 예외 (중복 제거됨)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Unhandled Exception]", e);

        // 수정 포인트: static factory method 사용
        ErrorResponse response = ErrorResponse.of(
                ResultCode.INTERNAL_SERVER_ERROR.getCode(),
                ResultCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
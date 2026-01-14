package com.example.greenribboncalimassignment.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,

        @JsonInclude(JsonInclude.Include.NON_EMPTY) // 비어있으면 JSON에서 제외 (깔끔함)
        List<ValidationError> errors
) {
    // 1. 일반 에러 응답 (errors = null)
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    // 2. Validation 에러 응답 (BindingResult 처리)
    public static ErrorResponse of(String code, String message, BindingResult bindingResult) {
        return new ErrorResponse(code, message, ValidationError.of(bindingResult));
    }

    // 내부 Inner Record
    public record ValidationError(
            String field,
            String value,
            String reason
    ) {
        // BindingResult -> List<ValidationError> 변환 로직
        public static List<ValidationError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new ValidationError(
                            error.getField(),
                            String.valueOf(error.getRejectedValue()),
                            error.getDefaultMessage()
                    ))
                    .toList(); // Java 16+
        }
    }
}
package com.example.greenribboncalimassignment.utils;

import com.example.greenribboncalimassignment.common.CodeValue;
import com.example.greenribboncalimassignment.web.dto.response.CodeResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumMapper {
    /**
     * CodeValue를 구현한 Enum 클래스를 리스트로 변환
     */
    public static List<CodeResponse> toResponses(Class<? extends CodeValue> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(CodeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 여러 Enum 클래스들을 한 번에 Map으로 묶음
     */
    public static Map<String, List<CodeResponse>> toMap(Map<String, Class<? extends CodeValue>> factory) {
        return factory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toResponses(e.getValue()),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }
}

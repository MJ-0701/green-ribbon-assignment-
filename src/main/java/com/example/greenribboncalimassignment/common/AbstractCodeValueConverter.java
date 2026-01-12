package com.example.greenribboncalimassignment.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

import java.util.EnumSet;

public abstract class AbstractCodeValueConverter<E extends Enum<E> & CodeValue>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected AbstractCodeValueConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    // Java Enum -> DB Data (한글 Description 저장)
    @Override
    public String convertToDatabaseColumn(E attribute) {
        if (attribute == null) return null;
        return attribute.getDescription(); // "대행 신청 완료" 저장
    }

    // DB Data -> Java Enum (한글을 다시 Enum으로 복구)
    @Override
    public E convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) return null;

        return EnumSet.allOf(enumClass).stream()
                .filter(e -> e.getDescription().equals(dbData))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Enum %s에 존재하지 않는 설명입니다: %s", enumClass.getSimpleName(), dbData)
                ));
    }
}

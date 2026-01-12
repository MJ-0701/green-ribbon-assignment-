package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.AbstractCodeValueConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GuaranteeTypeConverter extends AbstractCodeValueConverter<GuaranteeType> {
    public GuaranteeTypeConverter() {
        super(GuaranteeType.class);
    }
}

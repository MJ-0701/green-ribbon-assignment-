package com.example.greenribboncalimassignment.domain.proxy.entity;

import com.example.greenribboncalimassignment.common.AbstractCodeValueConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProxyStatusConverter extends AbstractCodeValueConverter<ProxyStatus> {
    public ProxyStatusConverter() {
        super(ProxyStatus.class);
    }
}

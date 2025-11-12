package com.gj.dev_note.common.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class EnumDtoConverterFactory implements ConverterFactory<String, Enum> {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        if (EnumDto.class.isAssignableFrom(targetType)) {
            return source -> castFromCode(source, targetType);
        }
        return source -> (T) Enum.valueOf((Class) targetType, source);
    }

    @SuppressWarnings("unchecked")
    private static <D, E extends Enum<E> & EnumDto<D, E>, T extends Enum>
    T castFromCode(String code, Class<T> raw) {
        return (T) EnumDto.fromCode(code, (Class<E>) raw);
    }
}

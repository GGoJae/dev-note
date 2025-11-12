package com.gj.dev_note.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface EnumDto<D , E extends Enum<E> & EnumDto<D, E>> extends CodeLabel<D>{

    @JsonValue
    default String json() {
        return code();
    }

    @SuppressWarnings("unchecked")
    default Class<E> enumType() {
        return (Class<E>) ((Enum<?>) this).getDeclaringClass();
    }

    default List<Option> options() {
        return options(enumType());
    }

    Map<Class<?>, Map<String, EnumDto<?, ?>>> BY_CODE = new ConcurrentHashMap<>();
    Map<Class<?>, Map<Object, EnumDto<?, ?>>> BY_DOMAIN = new ConcurrentHashMap<>();

    static <D, E extends Enum<E> & EnumDto<D, E>> E fromCode(String code, Class<E> type) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is required. allowed: " + allowedCodes(type));
        }
        var idx = BY_CODE.computeIfAbsent(type, EnumDto::indexByCode);
        @SuppressWarnings("unchecked")
        E hit = (E) idx.get(code);
        if (hit == null) {
            throw new IllegalArgumentException("unknown code: " + code + " (allowed: " + allowedCodes(type) + ")");
        }
        return hit;
    }

    static <D, E extends Enum<E> & EnumDto<D, E>> E fromType(D domain, Class<E> type) {
        var idx = BY_DOMAIN.computeIfAbsent(type, EnumDto::indexByDomain);
        @SuppressWarnings("unchecked")
        E hit = (E) idx.get(domain);
        if (hit == null) {
            throw new IllegalArgumentException("unsupported domain value: " + domain);
        }
        return hit;
    }

    static <D, E extends Enum<E> & EnumDto<D, E>> List<Option> options(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .map(Option::of)
                .collect(Collectors.toList());
    }

    static <D, E extends Enum<E> & EnumDto<D, E>> String allowedCodes(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .map(EnumDto::code)
                .collect(Collectors.joining(", "));
    }

    private static Map<String, EnumDto<?, ?>> indexByCode(Class<?> type) {
        Map<String, EnumDto<?, ?>> out = new HashMap<>();
        for (Object o : type.getEnumConstants()) {
            EnumDto<?, ?> e = (EnumDto<?, ?>) o;
            out.put(e.code(), e);
        }
        return out;
    }

    private static Map<Object, EnumDto<?, ?>> indexByDomain(Class<?> type) {
        Map<Object, EnumDto<?, ?>> out = new HashMap<>();
        for (Object o : type.getEnumConstants()) {
            EnumDto<?, ?> e = (EnumDto<?, ?>) o;
            out.put(e.toType(), e);
        }
        return out;
    }

    record Option(String code, String label) {
        public static Option of(CodeLabel<?> codeLabel) {
            return new Option(codeLabel.code(), codeLabel.label());
        }
    }

}

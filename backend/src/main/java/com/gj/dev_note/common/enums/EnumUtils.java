package com.gj.dev_note.common.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumUtils {

    public static <E extends Enum<E> & CodeLabel<?>> List<String> allowedCodes(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .map(CodeLabel::code)
                .toList();
    }

    public static <E extends Enum<E> & CodeLabel<?>> List<Option> options(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .map(Option::toOption)
                .toList();
    }

    public static <E extends Enum<E> & CodeLabel<?>> Map<String, E> byCodeIndex(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(CodeLabel::code, e -> e));
    }

    public record Option(String code, String label) {
        public static Option toOption(CodeLabel<?> codeLabel) {
            return new Option(codeLabel.code(), codeLabel.label());
        }
    }
}

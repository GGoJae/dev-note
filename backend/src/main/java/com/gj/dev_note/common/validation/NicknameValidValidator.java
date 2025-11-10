package com.gj.dev_note.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidValidator implements ConstraintValidator<NicknameValid, String> {
    private static final String PATTERN = "^[\\p{IsHangul}A-Za-z0-9 _.-]{2,30}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return false;
        String s = value.trim();
        return s.matches(PATTERN);
    }
}

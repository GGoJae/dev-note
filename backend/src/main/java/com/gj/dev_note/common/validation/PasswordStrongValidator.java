package com.gj.dev_note.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordStrongValidator implements ConstraintValidator<PasswordStrong, String> {

    private int min;

    @Override
    public void initialize(PasswordStrong ann) {
        this.min = ann.min();
    }

    @Override
    public boolean isValid(String raw, ConstraintValidatorContext ctx) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.length() < min) return false;

        int kinds = 0;
        if (s.matches(".*\\d.*")) kinds++;
        if (s.matches(".*[^A-Za-z0-9].*")) kinds++;

        return kinds >= 2;
    }
}

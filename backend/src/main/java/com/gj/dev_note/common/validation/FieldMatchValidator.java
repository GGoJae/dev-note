package com.gj.dev_note.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.security.core.parameters.P;

import java.lang.reflect.RecordComponent;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String first;
    private String second;

    @Override
    public void initialize(FieldMatch ann) {
        this.first = ann.first();
        this.second = ann.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        try {
            var v1 = read(value, first);
            var v2 = read(value, second);
            if (v1 == null && v2 == null) return true;

            return v1 != null && v1.equals(v2);
        } catch (Exception e) {
            return false;
        }
    }

    private Object read(Object target, String name) throws Exception {
        if (target.getClass().isRecord()) {
            for (var c : target.getClass().getRecordComponents()) {
                if (c.getName().equals(name)) {
                    return c.getAccessor().invoke(target);
                }
            }
            return null;
        }
        var f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}

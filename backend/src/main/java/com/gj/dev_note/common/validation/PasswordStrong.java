package com.gj.dev_note.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrongValidator.class)
public @interface PasswordStrong {
    String message() default "비밀번호 정책에 맞지 않습니다. (특수문자 섞어서 최소 10자)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 10;

}

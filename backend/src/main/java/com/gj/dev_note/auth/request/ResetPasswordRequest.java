package com.gj.dev_note.auth.request;

import com.gj.dev_note.common.validation.FieldMatch;
import com.gj.dev_note.common.validation.PasswordStrong;
import jakarta.validation.constraints.NotBlank;

@FieldMatch(first = "newPassword", second = "confirmPassword", message = "비밀번호 확인이 일치하지 않습니다.")
public record ResetPasswordRequest(
        @NotBlank String token,
        @PasswordStrong String newPassword,
        @NotBlank String confirmPassword
) {}

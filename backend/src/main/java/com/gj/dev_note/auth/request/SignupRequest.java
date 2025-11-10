package com.gj.dev_note.auth.request;

import com.gj.dev_note.common.validation.FieldMatch;
import com.gj.dev_note.common.validation.NicknameValid;
import com.gj.dev_note.common.validation.PasswordStrong;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldMatch(first = "password", second = "confirmPassword", message = "비밀번호 확인이 일치하지 않습니다.")
public record SignupRequest(
        @Email @NotBlank String email,
        @PasswordStrong String password,
        @NotBlank String confirmPassword,

        @NotBlank @Size(min = 2, max = 30)
        @NicknameValid String nickname
) {
}

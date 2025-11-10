package com.gj.dev_note.common.url;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppUrlProperties {
    private String publicBaseUrl = "http://localhost:8080";

    private String verifyEmailPath = "/api/auth/verify-email";
    private String resetPasswordPath = "/api/auth/reset-password";
}

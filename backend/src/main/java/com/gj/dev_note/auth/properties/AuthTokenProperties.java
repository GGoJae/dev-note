package com.gj.dev_note.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.token")
public class AuthTokenProperties {
    private long emailVerifyMinutes = 60;
    private long passwordResetMinutes = 30;
    private long refreshDays = 14;  // RT 만료
}

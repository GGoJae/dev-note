package com.gj.dev_note.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.password")
public class PasswordProps {
    private String pepper = "change-me-in-prod";
}

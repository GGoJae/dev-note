package com.gj.dev_note.config;

import com.gj.dev_note.artifact.dto.ArtifactGroupTypeDto;
import com.gj.dev_note.common.enums.EnumDtoConverterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final EnumDtoConverterFactory enumDtoConverterFactory;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(enumDtoConverterFactory);
    }
}

package com.gj.dev_note.config;

import com.gj.dev_note.artifact.dto.ArtifactGroupTypeDto;
import com.gj.dev_note.category.dto.CategoryScopeDto;
import com.gj.dev_note.common.VisibilityDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, ArtifactGroupTypeDto.class, ArtifactGroupTypeDto::from);
        registry.addConverter(String.class, CategoryScopeDto.class, CategoryScopeDto::from);
        registry.addConverter(String.class, VisibilityDto.class, VisibilityDto::from);
    }
}

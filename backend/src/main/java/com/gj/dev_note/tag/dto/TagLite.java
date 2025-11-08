package com.gj.dev_note.tag.dto;

public record TagLite(String slug, String name) {
    
    public static TagLite of(String slug, String name) {
        return new TagLite(slug, name);
    }
}

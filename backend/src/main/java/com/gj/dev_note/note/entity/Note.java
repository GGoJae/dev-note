package com.gj.dev_note.note.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    private String content;

    @Setter
    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private final Instant createdAt = Instant.now();

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private String title;
        private String content;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Note build() {
            return new Note(
                    null,
                    this.title,
                    this.content,
                    0
            );
        }
    }


}

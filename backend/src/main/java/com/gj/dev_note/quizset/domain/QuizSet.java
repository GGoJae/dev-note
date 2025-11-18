package com.gj.dev_note.quizset.domain;

import com.gj.dev_note.common.Visibility;
import com.gj.dev_note.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quiz_set", indexes = {
        @Index(name = "idx_qs_owner", columnList = "owner_id"),
        @Index(name = "idx_qs_visibility", columnList = "visibility"),
        @Index(name = "idx_qs_created", columnList = "createdAt")
})
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class QuizSet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="owner_id", nullable=false)
    private Member owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable=false, length=120)
    private String name;

    @Column(length=1000)
    private String description;

    @Column(nullable=false)
    @Builder.Default
    private int itemCount = 0;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable=false)
    private Instant updatedAt;

    public void incCount(int delta) {
        this.itemCount = Math.max(0, this.itemCount + delta);
    }
}

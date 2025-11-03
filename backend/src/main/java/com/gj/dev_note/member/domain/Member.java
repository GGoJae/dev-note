package com.gj.dev_note.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "member",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email", unique = true)
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class Member {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 150, unique = true)
        private String email;

        @Column(nullable = false, length = 200)
        private String passwordHash;

        @Column(nullable = false, length = 60)
        private String nickname;

        @ElementCollection(fetch = FetchType.EAGER, targetClass = Role.class)
        @CollectionTable(name = "member_roles",
                joinColumns = @JoinColumn(name="member_id"))
        @Enumerated(EnumType.STRING)
        @Column(name="role", nullable=false, length=20)
        @Builder.Default
        private Set<Role> roles = EnumSet.of(Role.USER);

        @CreationTimestamp
        @Column(nullable=false, updatable=false)
        private Instant createdAt;

        @UpdateTimestamp
        @Column(nullable=false)
        private Instant updatedAt;
}

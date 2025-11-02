package com.gj.dev_note.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "member",
        indexes = {
                @Index(name = "ux_member_email", columnList = "email", unique = true)
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
        private String password;

        @Column(nullable = false, length = 60)
        private String nickname;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
        @Enumerated(EnumType.STRING)
        @Column(name = "role", length = 40, nullable = false)
        private Set<Role> roles;

}

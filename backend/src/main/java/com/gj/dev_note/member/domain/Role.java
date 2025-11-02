package com.gj.dev_note.member.domain;

public enum Role {
    ADMIN, USER;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}

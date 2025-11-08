package com.gj.dev_note.member.dto;

import com.gj.dev_note.member.domain.Member;

public record MemberSummary(
        Long id,
        String nickname
) {
    public static MemberSummary fromDomain(Member member) {
        return new MemberSummary(member.getId(), member.getNickname());
    }
}

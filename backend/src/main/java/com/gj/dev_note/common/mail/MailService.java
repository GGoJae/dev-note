package com.gj.dev_note.common.mail;

public interface MailService {
    void send(String to, String subject, String body);
    default void sendEmailVerification(String to, String link) {
        send(to, "[DevNote] 이메일 인증 요청", "다음 링크를 클릭해 이메일을 인증하세요:\n" + link);
    }
    default void sendPasswordReset(String to, String link) {
        send(to, "[DevNote] 비밀번호 재설정", "다음 링크에서 비밀번호를 재설정하세요:\n" + link);
    }
}

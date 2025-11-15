package com.gj.dev_note.practice.dto;

public record NextHint(
        String type,
        String cursor,
        String finalizeToken
) {
    public static NextHint page(int nextOffset) {
        return new NextHint("page", String.valueOf(nextOffset), null);
    }
    public static NextHint finalize(String token) {
        return new NextHint("finalize", null, token);
    }
    public static NextHint none() {
        return new NextHint("none", null, null);
    }
}

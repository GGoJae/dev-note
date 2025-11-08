package com.gj.dev_note.common.enums;

public interface CodeLabel<D> {
    String code();
    String label();
    D toType();
}

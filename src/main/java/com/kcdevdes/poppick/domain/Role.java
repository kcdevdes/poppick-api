package com.kcdevdes.poppick.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("GUEST", "Guest"),
    USER("USER", "User"),
    ADMIN("ADMIN", "Admin");

    private final String key;
    private final String title;
}
package com.kcdevdes.poppick.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("GUEST", "Role_Guest"),
    USER("USER", "Role_User"),
    ADMIN("ADMIN", "Role_Admin");

    private final String key;
    private final String title;
}
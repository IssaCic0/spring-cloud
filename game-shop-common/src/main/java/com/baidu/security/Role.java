package com.baidu.security;

public enum Role {
    BUYER,
    MERCHANT,
    ADMIN;

    public static Role fromHeader(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}


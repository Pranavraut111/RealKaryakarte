package com.mandal.model;

/**
 * User roles in the Mandal system.
 */
public enum Role {
    ADMIN,
    KARYAKARTA,
    MEMBER;

    public static Role fromString(String value) {
        try {
            return Role.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}

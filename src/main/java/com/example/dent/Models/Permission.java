package com.example.dent.Models;

public enum Permission {
    USER("user"),
    DEVELOPER("dev");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}

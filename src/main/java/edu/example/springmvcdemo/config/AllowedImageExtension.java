package edu.example.springmvcdemo.config;

import java.util.Arrays;

public enum AllowedImageExtension {
    PNG,
    JPEG;

    public static boolean isAllowedIgnoreCase(String extension) {
        return Arrays.stream(AllowedImageExtension.values())
                .anyMatch(val -> val.name().equalsIgnoreCase(extension));
    }
}

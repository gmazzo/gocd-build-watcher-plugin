package com.github.gmazzo.utils;

public final class StringUtils {

    public static boolean isBlank(String text) {
        return text == null || text.matches("\\s*");
    }

}

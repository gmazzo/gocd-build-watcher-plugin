package com.github.gmazzo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}\\b", Pattern.CASE_INSENSITIVE);

    public static boolean isBlank(String text) {
        return text == null || text.matches("\\s*");
    }

    public static String capitalize(String text) {
        return isBlank(text) ? text : text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String extractEmail(String text) {
        Matcher m = EMAIL_PATTERN.matcher(text);
        return m.find() ? m.group() : null;
    }

}

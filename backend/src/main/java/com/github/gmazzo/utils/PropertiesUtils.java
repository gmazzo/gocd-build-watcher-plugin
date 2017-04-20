package com.github.gmazzo.utils;

import java.io.IOException;
import java.util.Properties;

public final class PropertiesUtils {

    public static Properties get() {
        try {
            Properties properties = new Properties();
            properties.load(PropertiesUtils.class.getResourceAsStream("/config.properties"));
            return properties;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

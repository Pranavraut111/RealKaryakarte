package com.mandal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides typed access to application.properties.
 * Singleton — initialized once, read many times.
 */
public class ConfigUtil {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("[ConfigUtil] application.properties not found on classpath!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private ConfigUtil() {}

    public static String get(String key) {
        // e.g. "db.url" -> "DB_URL"
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null || val.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        String val = get(key);
        if (val == null || val.trim().isEmpty()) return defaultValue;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }
}

package com.grincare.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHelper {

    private static final String CONFIG_FILE = "config.properties";
    private static Properties props;

    private static Properties getProperties() {
        if (props == null) {
            props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("[ConfigHelper] Gagal membaca " + CONFIG_FILE + ": " + e.getMessage());
            }
        }
        return props;
    }

    public static String getGeminiApiKey() {
        return getProperties().getProperty("gemini.api.key", "");
    }
}

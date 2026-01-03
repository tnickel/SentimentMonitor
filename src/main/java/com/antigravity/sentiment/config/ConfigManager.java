package com.antigravity.sentiment.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "settings.properties";
    private static final String KEY_ROOT_PATH = "root.path";
    private static final String KEY_THRESHOLD_UP = "threshold.up";
    private static final String KEY_THRESHOLD_DOWN = "threshold.down";

    private Properties properties;
    private File configFile;

    public ConfigManager() {
        properties = new Properties();
        ensureConfigDirExists();
        configFile = new File(CONFIG_DIR, CONFIG_FILE);
        loadConfig();
    }

    private void ensureConfigDirExists() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void loadConfig() {
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("Could not load config: " + e.getMessage());
            }
        }
    }

    public void saveConfig() {
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            properties.store(out, "Sentiment Monitor Settings");
        } catch (IOException e) {
            System.err.println("Could not save config: " + e.getMessage());
        }
    }

    public String getRootPath() {
        return properties.getProperty(KEY_ROOT_PATH);
    }

    public void setRootPath(String path) {
        properties.setProperty(KEY_ROOT_PATH, path);
        saveConfig();
    }

    public int getThresholdUp() {
        return Integer.parseInt(properties.getProperty(KEY_THRESHOLD_UP, "50"));
    }

    public void setThresholdUp(int value) {
        properties.setProperty(KEY_THRESHOLD_UP, String.valueOf(value));
        saveConfig();
    }

    public int getThresholdDown() {
        return Integer.parseInt(properties.getProperty(KEY_THRESHOLD_DOWN, "50"));
    }

    public void setThresholdDown(int value) {
        properties.setProperty(KEY_THRESHOLD_DOWN, String.valueOf(value));
        saveConfig();
    }
}

package dev.carlosrr.nebby.utils;

import java.io.*;
import java.util.Properties;

/**
 * Utility class to manage the most recent database file path.
 * This class saves and retrieves the path to the most recently loaded database file.
 */
public class RecentDBManager {
    
    private static final String PROPERTIES_FILE = "nebby.properties";
    private static final String RECENT_DB_KEY = "recent.db.file";
    
    /**
     * Saves the path to the most recently loaded database file.
     * 
     * @param dbFilePath The path to the database file
     */
    public static void saveRecentDBFilePath(String dbFilePath) {
        Properties properties = loadProperties();
        properties.setProperty(RECENT_DB_KEY, dbFilePath);
        saveProperties(properties);
    }
    
    /**
     * Retrieves the path to the most recently loaded database file.
     * 
     * @return The path to the most recently loaded database file, or null if none exists
     */
    public static String getRecentDBFilePath() {
        Properties properties = loadProperties();
        return properties.getProperty(RECENT_DB_KEY);
    }
    
    /**
     * Loads the properties from the properties file.
     * 
     * @return The properties object
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        File propertiesFile = new File(PROPERTIES_FILE);
        
        if (propertiesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading properties file: " + e.getMessage());
            }
        }
        
        return properties;
    }
    
    /**
     * Saves the properties to the properties file.
     * 
     * @param properties The properties to save
     */
    private static void saveProperties(Properties properties) {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(fos, "Nebby Application Properties");
        } catch (IOException e) {
            System.err.println("Error saving properties file: " + e.getMessage());
        }
    }
}
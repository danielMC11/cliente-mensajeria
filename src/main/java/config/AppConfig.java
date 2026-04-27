package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties;

    public AppConfig() {
        properties = new Properties();
        // Load default config or from file if needed.
        // For simplicity, we define defaults here.
        properties.setProperty("db.url", "jdbc:h2:tcp://localhost:9090/chat_db");
        properties.setProperty("db.user", "sa");
        properties.setProperty("db.password", "");
        properties.setProperty("db.port", "9090");
        properties.setProperty("db.dir", "./data");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

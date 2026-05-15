package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties;

    public AppConfig() {
        properties = new Properties();

        // Intentar cargar desde archivo en el classpath
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("app.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {}

        // Valores por defecto para H2 EMBEDDED (sin puerto TCP)
        // db.url NO se usa más directamente — H2Database construye la URL
        // a partir de db.dir, eliminando cualquier riesgo de conflicto de puertos.
        properties.putIfAbsent("db.user",     "sa");
        properties.putIfAbsent("db.password", "");
        properties.putIfAbsent("db.dir",      "./data");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

package data;

import config.AppConfig;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Database {
    private final String url;
    private final String user;
    private final String password;
    private final String port;
    private final String baseDir;
    
    private Server server;
    private boolean servidorExterno = false;

    public H2Database(AppConfig config) {
        this.url = config.getProperty("db.url");
        this.user = config.getProperty("db.user");
        this.password = config.getProperty("db.password");
        this.port = config.getProperty("db.port", "9090");
        this.baseDir = config.getProperty("db.dir", "./data");
        
        startServer();
        initDatabase();
    }

    private void startServer() {
        if (yaHayServidorCorriendo()) {
            System.out.println("H2: Servidor ya existente detectado, usando ese.");
            servidorExterno = true;
            return;
        }

        try {
            String absoluteBaseDir = new java.io.File(baseDir).getAbsolutePath();
            server = Server.createTcpServer(
                    "-tcp", "-tcpAllowOthers",
                    "-tcpPort", port,
                    "-ifNotExists",
                    "-baseDir", absoluteBaseDir
            ).start();
            servidorExterno = false;
            System.out.println("H2 Server iniciado en puerto " + port + ", baseDir: " + absoluteBaseDir);
        } catch (SQLException e) {
            System.err.println("Error iniciando H2 Server: " + e.getMessage());
        }
    }

    private boolean yaHayServidorCorriendo() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }

    public void stopServer() {
        if (servidorExterno) {
            System.out.println("H2: Servidor externo, no se detiene.");
            return;
        }
        if (server != null && server.isRunning(false)) {
            server.stop();
            System.out.println("H2 Server detenido.");
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void initDatabase() {
        String sqlMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255), " +
                "content TEXT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String sqlDocuments = "CREATE TABLE IF NOT EXISTS documents (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "filename VARCHAR(255), " +
                "file_size BIGINT, " +
                "extension VARCHAR(50), " +
                "mime_type VARCHAR(100), " +
                "username VARCHAR(255), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlMessages);
            stmt.execute(sqlDocuments);
            System.out.println("H2: Tablas verificadas/creadas correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
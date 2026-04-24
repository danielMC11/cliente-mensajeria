package config;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Database {

    private static final String URL = "jdbc:h2:tcp://localhost:9090/chat_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Server server;
    private static H2Database instance;
    private static boolean servidorExterno = false; // true si nos conectamos a uno ya existente

    private H2Database() {
        startServer();
        initDatabase();
    }

    public static synchronized H2Database getInstance() {
        if (instance == null) {
            instance = new H2Database();
        }
        return instance;
    }

    private static void startServer() {
        // Primero verificar si ya hay un servidor corriendo intentando conectarnos
        if (yaHayServidorCorriendo()) {
            System.out.println("H2: Servidor ya existente detectado en puerto 9090, usando ese.");
            servidorExterno = true;
            return;
        }

        // Si no hay ninguno, levantamos uno nuevo
        try {
            String baseDir = new java.io.File("./data").getAbsolutePath();

            server = Server.createTcpServer(
                    "-tcp",
                    "-tcpAllowOthers",
                    "-tcpPort", "9090",
                    "-ifNotExists",
                    "-baseDir", baseDir
            ).start();

            servidorExterno = false;
            System.out.println("H2 Server iniciado en puerto 9090, baseDir: " + baseDir);

        } catch (SQLException e) {
            System.err.println("Error iniciando H2 Server: " + e.getMessage());
        }
    }

    /**
     * Intenta conectarse al servidor H2 para verificar si ya está corriendo.
     * Si la conexión tiene éxito, hay un servidor activo.
     */
    private static boolean yaHayServidorCorriendo() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return conn != null;
        } catch (SQLException e) {
            return false; // No hay servidor corriendo
        }
    }

    /**
     * Solo detiene el servidor si fue este cliente quien lo inició.
     * Si era externo, no lo toca.
     */
    public static void stopServer() {
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
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void initDatabase() {
        String sqlMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255), " +
                "content TEXT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String sqlDocuments = "CREATE TABLE IF NOT EXISTS documents (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "filename VARCHAR(255), " +
                "file_size BIGINT, " +
                "extension VARCHAR(50), " +
                "mime_type VARCHAR(100), " +
                "username VARCHAR(255), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlMessages);
            stmt.execute(sqlDocuments);
            System.out.println("H2: Tablas verificadas/creadas correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveChatMessage(String username, String content) {
        String sql = "INSERT INTO messages (username, content) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar mensaje: " + e.getMessage());
        }
    }

    public void saveDocument(String filename, long size, String extension, String mimeType, String username) {
        String sql = "INSERT INTO documents (filename, file_size, extension, mime_type, username) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, filename);
            pstmt.setLong(2, size);
            pstmt.setString(3, extension);
            pstmt.setString(4, mimeType);
            pstmt.setString(5, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar documento: " + e.getMessage());
        }
    }
}
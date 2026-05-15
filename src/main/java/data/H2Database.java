package data;

import config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la base de datos H2 embebida del cliente.
 *
 * Modo EMBEDDED (archivo local):
 *   - No levanta ningún servidor TCP → elimina conflictos de puertos con el cluster P2P.
 *   - Los datos se guardan en ./data/chat_db.mv.db (ruta configurable via db.dir).
 *   - No requiere configurar host ni puerto; la conexión es directa al archivo.
 *
 * Cambio respecto a la versión anterior:
 *   - Se eliminó el Server.createTcpServer() que usaba el puerto 9092, el cual
 *     colisionaba con los puertos del cluster P2P (9090, 9091, 9092, 9093...).
 */
public class H2Database {
    private final String url;
    private final String user;
    private final String password;

    public H2Database(AppConfig config) {
        String baseDir = config.getProperty("db.dir", "./data");

        // URL embedded: jdbc:h2:./data/chat_db  (H2 crea el archivo automáticamente)
        // AUTO_SERVER=FALSE garantiza modo embedded (sin puerto TCP)
        this.url = "jdbc:h2:" + baseDir + "/chat_db"
                 + ";AUTO_SERVER=FALSE"
                 + ";AUTO_RECONNECT=TRUE"
                 + ";DB_CLOSE_DELAY=-1";   // mantener la BD abierta mientras la JVM vive
        this.user     = config.getProperty("db.user",     "sa");
        this.password = config.getProperty("db.password", "");

        initDatabase();
        System.out.println("H2 embedded iniciado — archivo: " + baseDir + "/chat_db.mv.db");
    }

    /** Crea las tablas si no existen. */
    private void initDatabase() {
        String sqlMessages = "CREATE TABLE IF NOT EXISTS messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(255), "
                + "content TEXT, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String sqlDocuments = "CREATE TABLE IF NOT EXISTS documents ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "filename VARCHAR(255), "
                + "file_size BIGINT, "
                + "extension VARCHAR(50), "
                + "mime_type VARCHAR(100), "
                + "username VARCHAR(255), "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlMessages);
            stmt.execute(sqlDocuments);
            System.out.println("H2: tablas verificadas/creadas correctamente.");
        } catch (SQLException e) {
            System.err.println("Error inicializando tablas H2: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * No-op: en modo embedded no hay servidor TCP que detener.
     * Se mantiene para compatibilidad con el código que llama stopServer().
     */
    public void stopServer() {
        System.out.println("H2 embedded: no hay servidor TCP que detener.");
    }
}
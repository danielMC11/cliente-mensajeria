import config.AppConfig;
import data.H2ChatRepository;
import data.H2Database;
import domain.ports.ChatRepository;
import ui.VentanaConexion;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // 1. Cargar Configuración
        AppConfig config = new AppConfig();
        
        // 2. Inicializar Base de Datos y Repositorio
        H2Database database = new H2Database(config);
        ChatRepository repository = new H2ChatRepository(database);
        
        // Callback para apagar la base de datos de forma limpia
        Runnable onDisconnect = database::stopServer;

        // 3. Iniciar la aplicación en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Inyectamos las dependencias
            VentanaConexion login = new VentanaConexion(repository, onDisconnect);
            login.setLocationRelativeTo(null); // Centrar en pantalla
            login.setVisible(true);
        });

        // Asegurarnos de que si la JVM se cierra abruptamente, se pare el server H2
        Runtime.getRuntime().addShutdownHook(new Thread(onDisconnect));
    }
}

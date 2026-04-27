import com.formdev.flatlaf.FlatMacDarkLaf;
import config.AppConfig;
import data.H2ChatRepository;
import data.H2Database;
import domain.ports.ChatRepository;
import ui.VentanaConexion;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Establecer Look & Feel FlatLaf (Premium UI)
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            // Algunas personalizaciones globales opcionales
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar FlatLaf.");
        }
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

package ui;

import javax.swing.*;
import java.awt.*;

import data.H2Database;
import data.H2ChatRepository;
import domain.ports.ChatRepository;
import network.ClientRouter;
import network.TCPClient;
import network.UDPClient;
import network.handlers.*;

public class VentanaConexion extends JFrame {
    public VentanaConexion() {
        setTitle("Conexión al Servidor");
        setSize(400, 320); // Aumentamos un poco el alto para el nuevo campo
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Aumentamos a 6 filas para dar espacio al nombre de usuario
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField txtUsername = new JTextField("Usuario1");
        JTextField txtIp = new JTextField("192.168.1.4");
        JTextField txtPort = new JTextField("8080");

        // --- CONFIGURACIÓN DE RADIO BUTTONS ---
        JRadioButton rbTcp = new JRadioButton("TCP", true); // Seleccionado por defecto
        JRadioButton rbUdp = new JRadioButton("UDP");

        // El ButtonGroup hace que la selección sea exclusiva
        ButtonGroup grupoProtocolo = new ButtonGroup();
        grupoProtocolo.add(rbTcp);
        grupoProtocolo.add(rbUdp);

        // Panel para agrupar los radios en una sola celda del GridLayout
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRadio.add(rbTcp);
        pnlRadio.add(rbUdp);

        JButton btnConectar = new JButton("Conectar");

        // --- AGREGAR COMPONENTES ---
        add(new JLabel("  Nombre de Usuario:"));
        add(txtUsername);

        add(new JLabel("  Dirección IP:"));
        add(txtIp);

        add(new JLabel("  Puerto:"));
        add(txtPort);

        add(new JLabel("  Protocolo:"));
        add(pnlRadio);

        // Espacio vacío para alinear el botón a la derecha
        add(new JLabel(""));
        add(btnConectar);

        btnConectar.addActionListener(e -> {
            String protocolo = rbTcp.isSelected() ? "TCP" : "UDP";
            String ip = txtIp.getText();
            int puerto = Integer.parseInt(txtPort.getText());
            String username = txtUsername.getText();

            if (protocolo.equals("TCP")) {
                new Thread(() -> {
                    try {
                        // 1. Setup DB y Repositorio
                        H2Database h2db = H2Database.getInstance();
                        ChatRepository repository = new H2ChatRepository(h2db);
                        
                        // 2. Setup Publisher y Router
                        SwingEventPublisher uiPublisher = new SwingEventPublisher();
                        ClientRouter router = new ClientRouter();
                        
                        router.registerHandler(new ConnectAckHandler(uiPublisher));
                        router.registerHandler(new ListClientsHandler(uiPublisher));
                        router.registerHandler(new ListLogsHandler(uiPublisher));
                        router.registerHandler(new ListMessagesHandler(uiPublisher));
                        router.registerHandler(new ListDocumentsHandler(uiPublisher));
                        router.registerHandler(new UploadInitAckHandler(uiPublisher));
                        router.registerHandler(new DownloadInitAckHandler(uiPublisher));
                        router.registerHandler(new UploadStatusHandler(uiPublisher, "UPLOAD_SUCCESS"));
                        router.registerHandler(new UploadStatusHandler(uiPublisher, "UPLOAD_FAILED"));

                        // 3. Setup Red
                        TCPClient client = new TCPClient(ip, puerto, username, repository, uiPublisher);
                        client.connect();
                        client.startListening(router);
                        
                        // 4. Setup UI
                        SwingUtilities.invokeLater(() -> {
                            Dashboard dashboard = new Dashboard(username, ip, String.valueOf(puerto), client, null);
                            uiPublisher.setDashboard(dashboard);
                            dashboard.setVisible(true);
                            this.dispose();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(this, "Error al conectar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).start();
            } else {
                // --- LÓGICA UDP ---
                new Thread(() -> {
                    try {
                        SwingEventPublisher uiPublisher = new SwingEventPublisher();
                        ClientRouter router = new ClientRouter();
                        
                        router.registerHandler(new ConnectAckHandler(uiPublisher));
                        router.registerHandler(new ListClientsHandler(uiPublisher));
                        router.registerHandler(new ListLogsHandler(uiPublisher));
                        router.registerHandler(new ListMessagesHandler(uiPublisher));
                        router.registerHandler(new ListDocumentsHandler(uiPublisher));

                        UDPClient udpClient = new UDPClient(ip, puerto, username, router);
                        udpClient.connect();

                        SwingUtilities.invokeLater(() -> {
                            Dashboard dashboard = new Dashboard(username, ip, String.valueOf(puerto), null, udpClient);
                            uiPublisher.setDashboard(dashboard);
                            dashboard.setVisible(true);
                            this.dispose();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "Error al preparar UDP: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).start();
            }
        });

        // Margen interno para que no pegue a los bordes de la ventana
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLocationRelativeTo(null);
    }
}

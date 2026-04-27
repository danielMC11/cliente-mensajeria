package ui;

import javax.swing.*;
import java.awt.*;

import domain.ports.ChatRepository;
import network.ClientRouter;
import network.TCPClient;
import network.UDPClient;
import network.handlers.*;

public class VentanaConexion extends JFrame {
    private final ChatRepository repository;
    private final Runnable onDisconnect;

    public VentanaConexion(ChatRepository repository, Runnable onDisconnect) {
        this.repository = repository;
        this.onDisconnect = onDisconnect;
        
        setTitle("Conexión al Servidor");
        setSize(400, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField txtUsername = new JTextField("Usuario1");
        JTextField txtIp = new JTextField("192.168.1.4");
        JTextField txtPort = new JTextField("8080");

        JRadioButton rbTcp = new JRadioButton("TCP", true);
        JRadioButton rbUdp = new JRadioButton("UDP");

        ButtonGroup grupoProtocolo = new ButtonGroup();
        grupoProtocolo.add(rbTcp);
        grupoProtocolo.add(rbUdp);

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRadio.add(rbTcp);
        pnlRadio.add(rbUdp);

        JButton btnConectar = new JButton("Conectar");

        add(new JLabel("  Nombre de Usuario:"));
        add(txtUsername);
        add(new JLabel("  Dirección IP:"));
        add(txtIp);
        add(new JLabel("  Puerto:"));
        add(txtPort);
        add(new JLabel("  Protocolo:"));
        add(pnlRadio);
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

                        TCPClient client = new TCPClient(ip, puerto, username, repository, uiPublisher);
                        client.connect();
                        client.startListening(router);
                        
                        SwingUtilities.invokeLater(() -> {
                            Dashboard dashboard = new Dashboard(username, ip, String.valueOf(puerto), client, null, onDisconnect, repository);
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
                            Dashboard dashboard = new Dashboard(username, ip, String.valueOf(puerto), null, udpClient, onDisconnect, repository);
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

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLocationRelativeTo(null);
    }
}

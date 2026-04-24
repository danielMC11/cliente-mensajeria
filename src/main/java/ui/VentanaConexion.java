package ui;

import javax.swing.*;
import java.awt.*;

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
                        config.TCPClient client = new config.TCPClient(ip, puerto, username);
                        client.connect();
                        
                        config.ClientRouter router = new config.ClientRouter();
                        client.startListening(router);
                        
                        // Si la conexión es exitosa, abrir el dashboard en el hilo de UI
                        SwingUtilities.invokeLater(() -> {
                            Dashboard dashboard = new Dashboard(username, ip, String.valueOf(puerto), client);
                            router.setDashboard(dashboard);
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
                // Lógica UDP pendiente o similar
                System.out.println("Conectando via UDP (No implementado)...");
                new Dashboard(username, ip, String.valueOf(puerto), null).setVisible(true);
                this.dispose();
            }
        });

        // Margen interno para que no pegue a los bordes de la ventana
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLocationRelativeTo(null);
    }
}

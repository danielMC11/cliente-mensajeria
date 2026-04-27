package ui;

import data.H2Database;
import network.TCPClient;
import network.UDPClient;
import ui.componentes.ComponenteClientes;
import ui.componentes.ComponenteLogs;
import ui.componentes.ComponenteTablaArchivos;
import ui.componentes.ComponenteTablaMensajes;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Dashboard extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel pnlCartas;
    private ComponenteClientes panelClientes;
    private ComponenteLogs panelLogs;
    private ComponenteTablaArchivos tablaArchivos;
    private ComponenteTablaMensajes tablaMensajes;
    private JRadioButton rbArchivos, rbMensajes;
    private TCPClient tcpClient;
    private JLabel lblStatus;
    private UDPClient udpClient;

    public Dashboard(String username, String ip, String puerto, TCPClient tcpClient, UDPClient udpClient) {
        this.tcpClient = tcpClient;
        this.udpClient = udpClient;
        setTitle("Dashboard - " + username + " conectado a " + ip);
        setSize(1250, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        panelClientes = new ComponenteClientes();
        panelLogs = new ComponenteLogs();

        pnlCartas = new JPanel(cardLayout);
        tablaArchivos = new ComponenteTablaArchivos();
        tablaMensajes = new ComponenteTablaMensajes();

        pnlCartas.add(tablaArchivos, "TABLA_ARCHIVOS");
        pnlCartas.add(tablaMensajes, "TABLA_MENSAJES");

        JPanel pnlHerramientas = crearPanelHerramientas();

        // Footer con status izquierda y desconectar derecha
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(230, 230, 230));

        lblStatus = new JLabel(" USUARIO: " + username + " | CONECTADO A: " + ip + " | PUERTO: " + puerto);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel pnlStatusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlStatusLeft.setBackground(new Color(230, 230, 230));
        pnlStatusLeft.add(lblStatus);

        JPanel pnlBtnRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtnRight.setBackground(new Color(230, 230, 230));
        pnlBtnRight.add(crearBotonDesconectar());

        pnlFooter.add(pnlStatusLeft, BorderLayout.WEST);
        pnlFooter.add(pnlBtnRight, BorderLayout.EAST);

        JPanel pnlCentroContenedor = new JPanel(new BorderLayout());
        pnlCentroContenedor.add(pnlHerramientas, BorderLayout.NORTH);
        pnlCentroContenedor.add(pnlCartas, BorderLayout.CENTER);

        add(panelClientes, BorderLayout.WEST);
        add(pnlCentroContenedor, BorderLayout.CENTER);
        add(panelLogs, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }
    public void enviarPeticion(String accion) {
        if (tcpClient != null) {
            // Si es TCP, usamos los métodos que ya tienes
            switch(accion) {
                case "LIST_CLIENTS": tcpClient.sendListClientsAction(); break;
                case "LIST_LOGS": tcpClient.sendListLogsAction(); break;
                case "LIST_DOCUMENTS": tcpClient.sendListDocumentsAction(); break;
                case "LIST_MESSAGES": tcpClient.sendListMessagesAction(); break;
            }
        } else if (udpClient != null) {
            // Si es UDP, usamos el nuevo método genérico
            udpClient.sendActionAsync(accion, new java.util.HashMap<>());
        }
    }
    private JButton crearBotonDesconectar() {
        JButton btnDesconectar = new JButton("Desconectar");
        btnDesconectar.setForeground(new Color(150, 0, 0));
        btnDesconectar.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnDesconectar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas cerrar la conexión?",
                    "Confirmar desconexión", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                        H2Database.stopServer();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new VentanaConexion().setVisible(true);
                this.dispose();
            }
        });

        return btnDesconectar;
    }

    public ComponenteClientes getPanelClientes() {
        return panelClientes;
    }

    public ComponenteLogs getPanelLogs() {
        return panelLogs;
    }

    public TCPClient getTcpClient() {
        return tcpClient;
    }

    public ComponenteTablaArchivos getTablaArchivos() {
        return tablaArchivos;
    }

    public ComponenteTablaMensajes getTablaMensajes() {
        return tablaMensajes;
    }

    public void iniciarDescarga(String docId, String filename, String format) {
        if (tcpClient != null) {
            tcpClient.requestDownload(docId, filename, format);
        }
    }

    private JPanel crearPanelHerramientas() {
        JPanel pnlPrincipal = new JPanel(new BorderLayout());
        JPanel pnlIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT));

        rbArchivos = new JRadioButton("Archivos", true);
        rbMensajes = new JRadioButton("Mensajes");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbArchivos);
        grupo.add(rbMensajes);

        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnEnviarArch = new JButton("Enviar Archivo");
        JButton btnEnviarMsg = new JButton("Enviar Mensaje");
        JButton btnRefresh = new JButton("Refrescar Tablas");

        btnFiltrar.addActionListener(e -> {
            if (rbArchivos.isSelected()) {
                cardLayout.show(pnlCartas, "TABLA_ARCHIVOS");
                enviarPeticion("LIST_DOCUMENTS"); // <-- CAMBIADO
            } else {
                cardLayout.show(pnlCartas, "TABLA_MENSAJES");
                enviarPeticion("LIST_MESSAGES");
            }
        });

        btnRefresh.addActionListener(e -> {
            if (rbArchivos.isSelected()) {
                enviarPeticion("LIST_DOCUMENTS");
            } else {
                enviarPeticion("LIST_MESSAGES");
            }
        });

        panelClientes.setRefreshAction(e -> enviarPeticion("LIST_CLIENTS")); // <-- CAMBIADO
        panelLogs.setRefreshAction(e -> enviarPeticion("LIST_LOGS")); // <-- CAMBIADO

        btnEnviarArch.addActionListener(e -> abrirVentanaEnvioArchivo());
        btnEnviarMsg.addActionListener(e -> abrirVentanaEnvioMensaje());

        pnlIzquierdo.add(rbArchivos);
        pnlIzquierdo.add(rbMensajes);
        pnlIzquierdo.add(btnFiltrar);
        pnlIzquierdo.add(new JSeparator(SwingConstants.VERTICAL));
        pnlIzquierdo.add(btnEnviarArch);
        pnlIzquierdo.add(btnEnviarMsg);
        pnlIzquierdo.add(btnRefresh);

        pnlPrincipal.add(pnlIzquierdo, BorderLayout.WEST);

        return pnlPrincipal;
    }

    private void abrirVentanaEnvioArchivo() {
        JDialog ventana = new JDialog(this, "Seleccionar archivo(s)", true);
        ventana.setLayout(new FlowLayout());
        JButton btnSeleccionar = new JButton("Seleccionar archivo(s)");
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        JLabel lblArchivo = new JLabel("Ningún archivo seleccionado");

        final File[][] archivosSeleccionados = {null};

        btnSeleccionar.addActionListener(e -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int option = fileChooser.showOpenDialog(ventana);
            if (option == JFileChooser.APPROVE_OPTION) {
                File[] seleccionados = fileChooser.getSelectedFiles();
                if (seleccionados == null || seleccionados.length == 0) {
                    File single = fileChooser.getSelectedFile();
                    if (single != null) seleccionados = new File[]{single};
                }
                if (seleccionados != null && seleccionados.length > 0) {
                    archivosSeleccionados[0] = seleccionados;
                    lblArchivo.setText(seleccionados.length + " archivo(s) seleccionado(s)");
                    btnEnviar.setEnabled(true);
                }
            }
        });

        btnEnviar.addActionListener(e -> {
            if (archivosSeleccionados[0] != null) {
                btnEnviar.setEnabled(false);
                btnEnviar.setText("Enviando...");
                for (File f : archivosSeleccionados[0]) {
                    if (tcpClient != null) {
                        tcpClient.sendFile(f);
                    }
                }
                ventana.dispose();
            }
        });

        ventana.add(btnSeleccionar);
        ventana.add(lblArchivo);
        ventana.add(btnEnviar);
        ventana.setSize(400, 150);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirVentanaEnvioMensaje() {
        JDialog ventana = new JDialog(this, "Enviar Mensaje", true);
        ventana.setLayout(new BorderLayout());
        JTextArea txtMsg = new JTextArea(5, 30);
        JButton btnEnviar = new JButton("Enviar");

        btnEnviar.addActionListener(e -> {
            String content = txtMsg.getText().trim();
            if (!content.isEmpty()) {
                if (tcpClient != null) {
                    tcpClient.sendChatMessage(content);
                    JOptionPane.showMessageDialog(ventana, "Mensaje enviado con éxito");
                    txtMsg.setText("");
                    ventana.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(ventana, "El mensaje no puede estar vacío");
            }
        });

        ventana.add(new JScrollPane(txtMsg), BorderLayout.CENTER);
        ventana.add(btnEnviar, BorderLayout.SOUTH);
        ventana.setSize(400, 250);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }
}
package ui;

import config.TCPClient;
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

    public Dashboard(String username, String ip, String puerto, TCPClient tcpClient) {
        this.tcpClient = tcpClient;
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

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFooter.setBackground(new Color(230, 230, 230));
        lblStatus = new JLabel(" USUARIO: " + username + " | CONECTADO A: " + ip + " | PUERTO: " + puerto);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        pnlFooter.add(lblStatus);

        JPanel pnlCentroContenedor = new JPanel(new BorderLayout());
        pnlCentroContenedor.add(pnlHerramientas, BorderLayout.NORTH);
        pnlCentroContenedor.add(pnlCartas, BorderLayout.CENTER);

        add(panelClientes, BorderLayout.WEST);
        add(pnlCentroContenedor, BorderLayout.CENTER);
        add(panelLogs, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
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
        // Usamos un BorderLayout interno para poder mandar el botón de desconectar a la
        // derecha
        JPanel pnlPrincipal = new JPanel(new BorderLayout());

        // Panel izquierdo para las opciones normales
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

        // --- BOTÓN DESCONECTAR ---
        JButton btnDesconectar = new JButton("Desconectar");
        btnDesconectar.setForeground(new Color(150, 0, 0)); // Color rojo oscuro para advertir
        btnDesconectar.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Lógica de intercambio de tablas y filtrado (ahora pide al servidor)
        btnFiltrar.addActionListener(e -> {
            if (rbArchivos.isSelected()) {
                cardLayout.show(pnlCartas, "TABLA_ARCHIVOS");
                tcpClient.sendListDocumentsAction();
            } else {
                cardLayout.show(pnlCartas, "TABLA_MENSAJES");
                tcpClient.sendListMessagesAction();
            }
        });

        // --- LÓGICA DE REFRESCO ---
        btnRefresh.addActionListener(e -> {
            if (rbArchivos.isSelected()) {
                tcpClient.sendListDocumentsAction();
            } else {
                tcpClient.sendListMessagesAction();
            }
        });

        panelClientes.setRefreshAction(e -> tcpClient.sendListClientsAction());
        panelLogs.setRefreshAction(e -> tcpClient.sendListLogsAction());

        // Lógica de desconexión
        btnDesconectar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas cerrar la conexión?",
                    "Confirmar desconexión", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new VentanaConexion().setVisible(true);
                this.dispose();
            }
        });

        btnEnviarArch.addActionListener(e -> abrirVentanaEnvioArchivo());
        btnEnviarMsg.addActionListener(e -> abrirVentanaEnvioMensaje());

        // Ensamblar panel izquierdo
        pnlIzquierdo.add(rbArchivos);
        pnlIzquierdo.add(rbMensajes);
        pnlIzquierdo.add(btnFiltrar);
        pnlIzquierdo.add(new JSeparator(SwingConstants.VERTICAL));
        pnlIzquierdo.add(btnEnviarArch);
        pnlIzquierdo.add(btnEnviarMsg);
        pnlIzquierdo.add(btnRefresh);

        // Panel derecho para Desconectar
        JPanel pnlDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDerecho.add(btnDesconectar);

        pnlPrincipal.add(pnlIzquierdo, BorderLayout.WEST);
        pnlPrincipal.add(pnlDerecho, BorderLayout.EAST);

        return pnlPrincipal;
    }

    private void abrirVentanaEnvioArchivo() {
        JDialog ventana = new JDialog(this, "Seleccionar archivo(s)", true);
        ventana.setLayout(new FlowLayout());
        JButton btnSeleccionar = new JButton("Seleccionar archivo(s)");
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        JLabel lblArchivo = new JLabel("Ningún archivo seleccionado");

        final File[][] archivosSeleccionados = { null };

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
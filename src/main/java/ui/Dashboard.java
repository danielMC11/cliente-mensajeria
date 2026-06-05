package ui;

import network.TCPClient;
import network.UDPClient;
import ui.componentes.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import domain.ports.ChatRepository;

/**
 * Ventana principal del cliente de mensajería P2P.
 *
 * Layout:
 *   WEST  → ComponenteClientes (lista federada LOCAL/REMOTO)
 *   CENTER → toolbar + JPanel con CardLayout (archivos / mensajes)
 *   EAST  → JTabbedPane con 3 tabs:
 *              - Logs (local)
 *              - Servidores (peers)
 *              - Logs Remotos
 *   SOUTH → barra de estado con notificaciones de cluster + botón desconectar
 *
 * Requerimientos cubiertos:
 *   ✓ Detección de servidores amigos (tab Servidores)
 *   ✓ Notificación push join/leave de servidores (barra de estado)
 *   ✓ Lista federada de clientes (ComponenteClientes con tipo LOCAL/REMOTO)
 *   ✓ Envío dirigido o broadcast (diálogo con selector de destinatario)
 *   ✓ Info y logs de otros servidores (tabs Servidores + Logs Remotos)
 */
public class Dashboard extends JFrame {

    // Layout
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlCartas;
    private final JRadioButton rbArchivos;
    private final JRadioButton rbMensajes;
    private JTabbedPane tabsPane;  // Campo para que los listeners de toolbar lo referencien

    // Componentes de datos
    private final ComponenteClientes    panelClientes;
    private final ComponenteLogs        panelLogs;
    private final ComponenteServidores  panelServidores;
    private final ComponentePeerLogs    panelPeerLogs;
    private final ComponenteTablaArchivos tablaArchivos;
    private final ComponenteTablaMensajes tablaMensajes;

    // Red
    private final TCPClient  tcpClient;
    private final UDPClient  udpClient;

    // Estado
    private final Runnable     onDisconnect;
    private final ChatRepository repository;
    private final String       username;

    // Barra de estado + notificaciones de cluster
    private final JLabel lblStatus;
    private final JLabel lblClusterEvent;  // muestra join/leave temporalmente

    // Guardar el último destinatario para mostrar el mensaje de confirmación
    private String ultimoDestinatarioMensaje;

    public Dashboard(String username, String ip, String puerto,
                     TCPClient tcpClient, UDPClient udpClient,
                     Runnable onDisconnect, ChatRepository repository) {
        this.tcpClient   = tcpClient;
        this.udpClient   = udpClient;
        this.onDisconnect = onDisconnect;
        this.repository  = repository;
        this.username    = username;

        setTitle("Dashboard P2P — " + username + " | " + ip + ":" + puerto);
        setSize(1400, 750);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                realizarDesconexion();
                System.exit(0);
            }
        });
        setLayout(new BorderLayout(4, 4));

        // ── Componentes ────────────────────────────────────────────────────
        panelClientes   = new ComponenteClientes();
        panelLogs       = new ComponenteLogs();
        panelServidores = new ComponenteServidores();
        panelPeerLogs   = new ComponentePeerLogs();
        tablaArchivos   = new ComponenteTablaArchivos();
        tablaMensajes   = new ComponenteTablaMensajes();

        // ── Centro: CardLayout ──────────────────────────────────────────────
        pnlCartas = new JPanel(cardLayout);
        pnlCartas.add(tablaArchivos, "TABLA_ARCHIVOS");
        pnlCartas.add(tablaMensajes, "TABLA_MENSAJES");

        rbArchivos = new JRadioButton("Archivos", true);
        rbMensajes = new JRadioButton("Mensajes");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbArchivos);
        grupo.add(rbMensajes);

        // ── Este: JTabbedPane con 3 tabs ───────────────────────────────────
        tabsPane = new JTabbedPane();   // Asignamos al campo de instancia
        tabsPane.setPreferredSize(new Dimension(320, 0));
        tabsPane.addTab("📋 Logs", panelLogs);
        tabsPane.addTab("🌐 Servidores", panelServidores);
        tabsPane.addTab("📡 Logs Remotos", panelPeerLogs);

        // Acciones de los botones de refresh de cada tab
        panelLogs.setRefreshAction(e -> enviarPeticion("LIST_LOGS"));
        panelServidores.setRefreshAction(e -> enviarPeticion("LIST_PEER_INFO"));
        panelPeerLogs.setRefreshAction(e -> enviarPeticion("LIST_PEER_LOGS"));

        // ── Centro: toolbar + cartas (después de tabsPane para que los listeners funcionen)
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.add(crearToolbar(), BorderLayout.NORTH);
        pnlCentro.add(pnlCartas, BorderLayout.CENTER);

        // ── Sur: barra de estado ────────────────────────────────────────────
        lblStatus = new JLabel("  👤 " + username + "  |  🔗 " + ip + ":" + puerto);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));

        lblClusterEvent = new JLabel("  🔵 Cluster activo");
        lblClusterEvent.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblClusterEvent.setForeground(new Color(0, 80, 160));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlLeft.add(lblStatus);
        pnlLeft.add(new JSeparator(SwingConstants.VERTICAL));
        pnlLeft.add(lblClusterEvent);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBorder(new EmptyBorder(2, 4, 2, 4));
        pnlFooter.add(pnlLeft, BorderLayout.WEST);
        pnlFooter.add(crearBotonDesconectar(), BorderLayout.EAST);

        // ── Ensamblado ──────────────────────────────────────────────────────
        add(panelClientes,  BorderLayout.WEST);
        add(pnlCentro,      BorderLayout.CENTER);
        add(tabsPane,       BorderLayout.EAST);
        add(pnlFooter,      BorderLayout.SOUTH);

        // Refresh inicial de clientes
        panelClientes.setRefreshAction(e -> enviarPeticion("LIST_CLIENTS"));

        setLocationRelativeTo(null);
    }

    // ── Toolbar ─────────────────────────────────────────────────────────────

    private JPanel crearToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        JButton btnFiltrar     = new JButton("Filtrar");
        JButton btnEnviarArch  = new JButton("📁 Enviar Archivo");
        JButton btnEnviarMsg   = new JButton("✉ Enviar Mensaje");
        JButton btnRefresh     = new JButton("⟳ Refrescar");
        JButton btnServidores  = new JButton("🌐 Info Servidores");
        JButton btnLogsRemotos = new JButton("📡 Logs Remotos");

        btnFiltrar.addActionListener(e -> {
            if (rbArchivos.isSelected()) {
                cardLayout.show(pnlCartas, "TABLA_ARCHIVOS");
                enviarPeticion("LIST_DOCUMENTS");
            } else {
                cardLayout.show(pnlCartas, "TABLA_MENSAJES");
                enviarPeticion("LIST_MESSAGES");
            }
        });

        btnRefresh.addActionListener(e -> {
            enviarPeticion(rbArchivos.isSelected() ? "LIST_DOCUMENTS" : "LIST_MESSAGES");
        });

        btnEnviarArch.addActionListener(e -> abrirVentanaEnvioArchivo());
        btnEnviarMsg.addActionListener(e -> abrirVentanaEnvioMensaje());

        btnServidores.addActionListener(e -> {
            enviarPeticion("LIST_PEER_INFO");
            if (tabsPane != null) tabsPane.setSelectedIndex(1);
        });

        btnLogsRemotos.addActionListener(e -> {
            enviarPeticion("LIST_PEER_LOGS");
            if (tabsPane != null) tabsPane.setSelectedIndex(2);
        });

        panel.add(rbArchivos);
        panel.add(rbMensajes);
        panel.add(btnFiltrar);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnEnviarArch);
        panel.add(btnEnviarMsg);
        panel.add(btnRefresh);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnServidores);
        panel.add(btnLogsRemotos);

        return panel;
    }

    // ── Envío de peticiones ─────────────────────────────────────────────────

    public void enviarPeticion(String accion) {
        if (tcpClient != null) {
            switch (accion) {
                case "LIST_CLIENTS":    tcpClient.sendListClientsAction();    break;
                case "LIST_LOGS":       tcpClient.sendListLogsAction();       break;
                case "LIST_DOCUMENTS":  tcpClient.sendListDocumentsAction();  break;
                case "LIST_MESSAGES":   tcpClient.sendListMessagesAction();   break;
                case "LIST_PEER_INFO":  tcpClient.sendListPeerInfoAction();   break;
                case "LIST_PEER_LOGS":  tcpClient.sendListPeerLogsAction();   break;
            }
        } else if (udpClient != null) {
            udpClient.sendActionAsync(accion, new java.util.HashMap<>());
        }
    }


    // ── Notificación de cluster ─────────────────────────────────────────────

    /**
     * Muestra un mensaje de evento de cluster en la barra de estado con color.
     * El mensaje se borra automáticamente después de 5 segundos.
     */
    public void showClusterNotification(String message, Color color) {
        lblClusterEvent.setText("  " + message);
        lblClusterEvent.setForeground(color);

        // Restaurar después de 5 segundos
        Timer timer = new Timer(5000, e -> {
            lblClusterEvent.setText("  🔵 Cluster activo");
            lblClusterEvent.setForeground(new Color(0, 80, 160));
        });
        timer.setRepeats(false);
        timer.start();
    }

    // ── Diálogos ────────────────────────────────────────────────────────────

    private void abrirVentanaEnvioArchivo() {
        JDialog ventana = new JDialog(this, "Enviar Archivo(s)", true);
        ventana.setLayout(new BorderLayout(8, 8));
        ventana.getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Selector de destinatario
        List<String> opciones = new ArrayList<>();
        opciones.add("— Todos —");
        opciones.addAll(panelClientes.getCurrentUsernames());

        JComboBox<String> cmbDestinatario = new JComboBox<>(opciones.toArray(new String[0]));
        cmbDestinatario.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel pnlDest = new JPanel(new BorderLayout(4, 0));
        pnlDest.add(new JLabel("Destinatario: "), BorderLayout.WEST);
        pnlDest.add(cmbDestinatario, BorderLayout.CENTER);

        JButton btnSeleccionar = new JButton("Seleccionar archivo(s)");
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        JLabel lblArchivo = new JLabel("Ningún archivo seleccionado");
        lblArchivo.setHorizontalAlignment(SwingConstants.CENTER);
        final java.io.File[][] archivosSeleccionados = {null};

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                java.io.File[] sel = fc.getSelectedFiles();
                if (sel == null || sel.length == 0) {
                    java.io.File s = fc.getSelectedFile();
                    if (s != null) sel = new java.io.File[]{s};
                }
                if (sel != null && sel.length > 0) {
                    archivosSeleccionados[0] = sel;
                    lblArchivo.setText(sel.length + " archivo(s) seleccionado(s)");
                    btnEnviar.setEnabled(true);
                }
            }
        });

        btnEnviar.addActionListener(e -> {
            if (archivosSeleccionados[0] != null && (tcpClient != null || udpClient != null)) {
                btnEnviar.setEnabled(false);
                btnEnviar.setText("Enviando...");
                String target = (String) cmbDestinatario.getSelectedItem();
                String finalTarget = "— Todos —".equals(target) ? "ALL" : target;
                
                for (java.io.File f : archivosSeleccionados[0]) {
                    if (tcpClient != null) {
                        tcpClient.sendFile(f, finalTarget);
                    } else if (udpClient != null) {
                        udpClient.sendFile(f, finalTarget);
                    }
                }

                ventana.dispose();
            }
        });

        JPanel pnlCenter = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlCenter.add(btnSeleccionar);
        pnlCenter.add(lblArchivo);

        ventana.add(pnlDest, BorderLayout.NORTH);
        ventana.add(pnlCenter, BorderLayout.CENTER);
        ventana.add(btnEnviar, BorderLayout.SOUTH);
        ventana.setSize(400, 200);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    /**
     * Diálogo de envío de mensaje con selector de destinatario.
     *
     * Permite elegir entre:
     *   - "— Todos —"  → broadcast a todos los clientes de la red
     *   - Un cliente específico → entrega dirigida (SEND_MESSAGE con targetUsername)
     *
     * Requerimiento: "Los documentos/mensajes se podrán enviar a un cliente en especial o a todos."
     */
    private void abrirVentanaEnvioMensaje() {
        if (tcpClient == null && udpClient == null) return;

        JDialog ventana = new JDialog(this, "Enviar Mensaje", true);
        ventana.setLayout(new BorderLayout(8, 8));
        ventana.getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Selector de destinatario
        List<String> opciones = new ArrayList<>();
        opciones.add("— Todos —");
        opciones.addAll(panelClientes.getCurrentUsernames());

        JComboBox<String> cmbDestinatario = new JComboBox<>(opciones.toArray(new String[0]));
        cmbDestinatario.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel pnlDest = new JPanel(new BorderLayout(4, 0));
        pnlDest.add(new JLabel("Destinatario: "), BorderLayout.WEST);
        pnlDest.add(cmbDestinatario, BorderLayout.CENTER);

        // Área de texto
        JTextArea txtMsg = new JTextArea(5, 30);
        txtMsg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);

        // Botón enviar
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnEnviar.addActionListener(e -> {
            String content = txtMsg.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(ventana, "El mensaje no puede estar vacío.");
                return;
            }
            String target = (String) cmbDestinatario.getSelectedItem();
            ultimoDestinatarioMensaje = target;
            if ("— Todos —".equals(target)) {
                if (tcpClient != null) tcpClient.sendDirectMessage("ALL", content);
                else if (udpClient != null) udpClient.sendDirectMessage("ALL", content);
            } else {
                if (tcpClient != null) tcpClient.sendDirectMessage(target, content);
                else if (udpClient != null) udpClient.sendDirectMessage(target, content);
            }
            txtMsg.setText("");
            ventana.dispose();
        });

        JPanel pnlTop = new JPanel(new BorderLayout(4, 4));
        pnlTop.add(pnlDest, BorderLayout.NORTH);

        ventana.add(pnlTop, BorderLayout.NORTH);
        ventana.add(new JScrollPane(txtMsg), BorderLayout.CENTER);
        ventana.add(btnEnviar, BorderLayout.SOUTH);
        ventana.setSize(450, 280);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    public void showSendMessageAck(String status, String message) {
        if ("SUCCESS".equals(status)) {
            String target = ultimoDestinatarioMensaje;
            if (target != null) {
                if ("— Todos —".equals(target)) {
                    JOptionPane.showMessageDialog(this, "Mensaje enviado a todos.");
                } else {
                    JOptionPane.showMessageDialog(this, "Mensaje enviado a " + target + ".");
                }
            } else {
                JOptionPane.showMessageDialog(this, message, "Mensaje Enviado", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar: " + message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Botón desconectar ────────────────────────────────────────────────────

    private JButton crearBotonDesconectar() {
        JButton btn = new JButton("Desconectar");
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas cerrar la conexión?",
                    "Confirmar desconexión", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                realizarDesconexion();
                new VentanaConexion(repository, onDisconnect).setVisible(true);
                this.dispose();
            }
        });
        return btn;
    }

    private void realizarDesconexion() {
        try {
            if (tcpClient != null) tcpClient.disconnect();
            if (udpClient != null) udpClient.disconnect();
            if (onDisconnect != null) onDisconnect.run();
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
    }

    // ── Getters para SwingEventPublisher ─────────────────────────────────────

    /** Inicia la descarga de un documento (llamado desde EditorGenerico). */
    public void iniciarDescarga(String docId, String filename, String format) {
        if (tcpClient != null) {
            tcpClient.requestDownload(docId, filename, format);
        } else if (udpClient != null) {
            udpClient.requestDownload(docId, filename, format);
        }
    }

    private String pendingAnalyzeId;
    public void setPendingAnalyzeId(String id) { this.pendingAnalyzeId = id; }
    public String getPendingAnalyzeId() { return this.pendingAnalyzeId; }

    public void analizarMensaje(String docId, String contenido) {
        setPendingAnalyzeId(docId);
        if (tcpClient != null) {
            tcpClient.sendAnalyzeMessage(contenido);
        } else if (udpClient != null) {
            udpClient.sendAnalyzeMessage(contenido);
        }
    }

    public ComponenteClientes   getPanelClientes()   { return panelClientes; }
    public ComponenteLogs       getPanelLogs()       { return panelLogs; }
    public ComponenteServidores getPanelServidores() { return panelServidores; }
    public ComponentePeerLogs   getPanelPeerLogs()   { return panelPeerLogs; }
    public ComponenteTablaArchivos getTablaArchivos()  { return tablaArchivos; }
    public ComponenteTablaMensajes getTablaMensajes()  { return tablaMensajes; }
    public TCPClient getTcpClient() { return tcpClient; }
    public UDPClient getUdpClient() { return udpClient; }
}
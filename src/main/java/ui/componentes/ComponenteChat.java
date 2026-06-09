package ui.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ComponenteChat extends JPanel {

    public interface EnviarMensajeListener {
        void onEnviarMensaje(String targetUsername, String contenido);
    }

    private static final Color COLOR_PROPIO_BG     = new Color(37, 99, 235);
    private static final Color COLOR_PROPIO_FG     = Color.WHITE;
    private static final Color COLOR_AJENO_BG      = new Color(241, 245, 249);
    private static final Color COLOR_AJENO_FG      = new Color(30, 40, 60);
    private static final Color COLOR_EMISOR_PROPIO = new Color(186, 210, 255);
    private static final Color COLOR_EMISOR_AJENO  = new Color(100, 116, 139);
    private static final Color COLOR_CHAT_BG       = new Color(248, 250, 252);

    private static final Font FUENTE_EMISOR  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FUENTE_MENSAJE = new Font("Segoe UI", Font.PLAIN, 13);

    private static final int ESPACIO_ENTRE_BURBUJAS = 8;
    private static final int MAX_BURBUJA_PX = 420;

    private final JPanel      pnlBurbujas;
    private final JScrollPane scroll;
    private final JTextArea   txtMensaje;
    private final JButton     btnEnviar;
    private final JComboBox<String> cmbDestinatario;

    private String username;
    private EnviarMensajeListener enviarListener;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public ComponenteChat() {
        setLayout(new BorderLayout(0, 0));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        JLabel lblTitulo = new JLabel("\uD83D\uDCAC Chat General");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(30, 40, 70));
        header.add(lblTitulo);

        // Panel burbujas
        pnlBurbujas = new JPanel();
        pnlBurbujas.setLayout(new BoxLayout(pnlBurbujas, BoxLayout.Y_AXIS));
        pnlBurbujas.setBackground(COLOR_CHAT_BG);
        pnlBurbujas.setBorder(new EmptyBorder(12, 12, 12, 12));

        scroll = new JScrollPane(pnlBurbujas);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(COLOR_CHAT_BG);

        // Input
        JPanel pnlInput = new JPanel(new BorderLayout(6, 0));
        pnlInput.setBackground(Color.WHITE);
        pnlInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(8, 10, 8, 10)));

        cmbDestinatario = new JComboBox<>();
        cmbDestinatario.addItem("\u2014 Todos \u2014");
        cmbDestinatario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbDestinatario.setPreferredSize(new Dimension(140, 32));

        txtMensaje = new JTextArea(2, 30);
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        txtMensaje.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "send");
        txtMensaje.getActionMap().put("send", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { enviarMensaje(); }
        });
        txtMensaje.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("shift ENTER"), "newline");
        txtMensaje.getActionMap().put("newline", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { txtMensaje.append("\n"); }
        });

        btnEnviar = new JButton("Enviar \u27A4") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(29, 78, 216) : new Color(37, 99, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnEnviar.addActionListener(e -> enviarMensaje());

        JPanel pnlLeft = new JPanel(new BorderLayout(6, 0));
        pnlLeft.setOpaque(false);
        pnlLeft.add(cmbDestinatario, BorderLayout.WEST);
        pnlLeft.add(new JScrollPane(txtMensaje), BorderLayout.CENTER);

        pnlInput.add(pnlLeft, BorderLayout.CENTER);
        pnlInput.add(btnEnviar, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(pnlInput, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────
    public void setUsername(String username) {
        this.username = username;
    }

    public void actualizarDestinatarios(List<String> usernames) {
        String selected = (String) cmbDestinatario.getSelectedItem();
        cmbDestinatario.removeAllItems();
        cmbDestinatario.addItem("\u2014 Todos \u2014");
        if (usernames != null) {
            for (String u : usernames) cmbDestinatario.addItem(u);
        }
        if (selected != null) cmbDestinatario.setSelectedItem(selected);
    }

    public void updateMessages(List<Map<String, Object>> mensajes) {
        pnlBurbujas.removeAll();

        if (mensajes != null) {
            for (int i = 0; i < mensajes.size(); i++) {
                Map<String, Object> m = mensajes.get(i);
                String  emisor   = obtenerEmisor(m);
                String  contenido = String.valueOf(m.getOrDefault("contenido", ""));
                boolean esMio    = emisor != null && emisor.equals(this.username);

                pnlBurbujas.add(crearFilaBurbuja(emisor, contenido, esMio));

                // Separador fijo entre burbujas
                if (i < mensajes.size() - 1) {
                    JPanel sep = new JPanel();
                    sep.setOpaque(false);
                    sep.setPreferredSize(new Dimension(0, ESPACIO_ENTRE_BURBUJAS));
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, ESPACIO_ENTRE_BURBUJAS));
                    sep.setMinimumSize(new Dimension(0, ESPACIO_ENTRE_BURBUJAS));
                    sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                    pnlBurbujas.add(sep);
                }
            }
        }

        pnlBurbujas.revalidate();
        pnlBurbujas.repaint();
        scrollAlFinal();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Construcción de burbuja
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel crearFilaBurbuja(String emisor, String contenido, boolean esMio) {

        // Etiqueta emisor
        JLabel lblEmisor = new JLabel(esMio ? "T\u00FA" : emisor);
        lblEmisor.setFont(FUENTE_EMISOR);
        lblEmisor.setForeground(esMio ? COLOR_EMISOR_PROPIO : COLOR_EMISOR_AJENO);

        // Texto con max-width para que la burbuja se ajuste al contenido
        String htmlTexto = "<html><div style='max-width:" + MAX_BURBUJA_PX + "px; word-wrap:break-word;'>"
                + contenido.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>")
                + "</div></html>";

        JLabel lblMsg = new JLabel(htmlTexto);
        lblMsg.setFont(FUENTE_MENSAJE);
        lblMsg.setForeground(esMio ? COLOR_PROPIO_FG : COLOR_AJENO_FG);

        // Burbuja redondeada
        JPanel burbuja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        burbuja.setLayout(new BorderLayout(0, 4));
        burbuja.setOpaque(false);
        burbuja.setBackground(esMio ? COLOR_PROPIO_BG : COLOR_AJENO_BG);
        burbuja.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        burbuja.add(lblEmisor, BorderLayout.NORTH);
        burbuja.add(lblMsg,    BorderLayout.CENTER);

        // Wrapper interno: FlowLayout solo para alinear la burbuja (sin gaps verticales)
        JPanel wrapper = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(burbuja);

        // Fila exterior: BorderLayout para ocupar todo el ancho sin gaps
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.add(wrapper, BorderLayout.CENTER);

        // ── CLAVE: limitar la altura al contenido real para que BoxLayout
        //    no añada espacio vertical extra entre filas ─────────────────────
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));

        return fila;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private String obtenerEmisor(Map<String, Object> m) {
        for (String key : new String[]{"propietario", "username", "emisor"}) {
            Object v = m.get(key);
            if (v != null) return v.toString();
        }
        return "?";
    }

    private void enviarMensaje() {
        String contenido = txtMensaje.getText().trim();
        if (contenido.isEmpty()) return;
        String target      = (String) cmbDestinatario.getSelectedItem();
        String finalTarget = "\u2014 Todos \u2014".equals(target) ? null : target;
        if (enviarListener != null) enviarListener.onEnviarMensaje(finalTarget, contenido);
        txtMensaje.setText("");
    }

    private void scrollAlFinal() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vBar = scroll.getVerticalScrollBar();
            vBar.setValue(vBar.getMaximum());
        });
    }

    public void setEnviarListener(EnviarMensajeListener listener) {
        this.enviarListener = listener;
    }
}
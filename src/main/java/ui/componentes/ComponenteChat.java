package ui.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Chat general con burbujas estilo mensajería moderna.
 * Los mensajes propios se muestran a la derecha (azul),
 * los ajenos a la izquierda (gris).
 */
public class ComponenteChat extends JPanel {

    /** Listener para enviar mensajes. */
    public interface EnviarMensajeListener {
        void onEnviarMensaje(String targetUsername, String contenido);
    }

    private static final Color COLOR_BURBUJA_PROPIA = new Color(220, 235, 255);
    private static final Color COLOR_BURBUJA_AJENA = new Color(240, 240, 240);
    private static final Color COLOR_BORDE_PROPIA = new Color(160, 195, 240);
    private static final Color COLOR_BORDE_AJENA = new Color(210, 210, 210);

    private final JPanel pnlBurbujas;
    private final JTextArea txtMensaje;
    private final JButton btnEnviar;
    private final JComboBox<String> cmbDestinatario;

    private String username; // current user
    private EnviarMensajeListener enviarListener;

    public ComponenteChat() {
        setLayout(new BorderLayout(0, 0));

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        header.setBorder(new EmptyBorder(4, 8, 4, 8));
        JLabel lblTitulo = new JLabel("💬 Chat General");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(lblTitulo);

        // --- Bubbles area ---
        pnlBurbujas = new JPanel();
        pnlBurbujas.setLayout(new BoxLayout(pnlBurbujas, BoxLayout.Y_AXIS));
        pnlBurbujas.setBackground(Color.WHITE);
        pnlBurbujas.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(pnlBurbujas);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // --- Input area ---
        JPanel pnlInput = new JPanel(new BorderLayout(6, 0));
        pnlInput.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Destinatario selector
        cmbDestinatario = new JComboBox<>();
        cmbDestinatario.addItem("— Todos —");
        cmbDestinatario.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbDestinatario.setPreferredSize(new Dimension(140, 30));

        txtMensaje = new JTextArea(2, 30);
        txtMensaje.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        // Send with Enter (Shift+Enter for newline)
        txtMensaje.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "send");
        txtMensaje.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enviarMensaje();
            }
        });
        txtMensaje.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("shift ENTER"), "newline");
        txtMensaje.getActionMap().put("newline", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtMensaje.append("\n");
            }
        });

        btnEnviar = new JButton("Enviar ➤");
        btnEnviar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnEnviar.setBackground(new Color(52, 120, 246));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnEnviar.addActionListener(e -> enviarMensaje());

        JPanel pnlRight = new JPanel(new BorderLayout(4, 0));
        pnlRight.add(btnEnviar, BorderLayout.CENTER);

        JPanel pnlLeft = new JPanel(new BorderLayout(4, 0));
        pnlLeft.add(cmbDestinatario, BorderLayout.WEST);
        pnlLeft.add(new JScrollPane(txtMensaje), BorderLayout.CENTER);

        pnlInput.add(pnlLeft, BorderLayout.CENTER);
        pnlInput.add(pnlRight, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pnlInput, BorderLayout.SOUTH);
    }

    /** Sets the current username (to distinguish own messages). */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Updates the list of available recipients. */
    public void actualizarDestinatarios(List<String> usernames) {
        String selected = (String) cmbDestinatario.getSelectedItem();
        cmbDestinatario.removeAllItems();
        cmbDestinatario.addItem("— Todos —");
        if (usernames != null) {
            for (String u : usernames) {
                cmbDestinatario.addItem(u);
            }
        }
        if (selected != null) {
            cmbDestinatario.setSelectedItem(selected);
        }
    }

    /**
     * Renders all messages as chat bubbles.
     * Each message map should have: propietario, contenido
     */
    public void updateMessages(List<Map<String, Object>> mensajes) {
        pnlBurbujas.removeAll();
        if (mensajes != null) {
            for (Map<String, Object> m : mensajes) {
                String emisor = (String) m.getOrDefault("propietario", "?");
                String contenido = (String) m.getOrDefault("contenido", "");
                boolean esMio = emisor != null && emisor.equals(this.username);
                agregarBurbuja(emisor, contenido, esMio);
            }
        }
        pnlBurbujas.revalidate();
        pnlBurbujas.repaint();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, pnlBurbujas);
            if (scrollPane != null) {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                vBar.setValue(vBar.getMaximum());
            }
        });
    }

    private void agregarBurbuja(String emisor, String contenido, boolean esMio) {
        JPanel burbuja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        burbuja.setLayout(new BorderLayout(4, 2));
        burbuja.setOpaque(false);
        burbuja.setBackground(esMio ? COLOR_BURBUJA_PROPIA : COLOR_BURBUJA_AJENA);
        burbuja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(esMio ? COLOR_BORDE_PROPIA : COLOR_BORDE_AJENA, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel lblEmisor = new JLabel(esMio ? "Tú" : emisor);
        lblEmisor.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblEmisor.setForeground(esMio ? new Color(40, 90, 170) : new Color(80, 80, 80));

        JTextArea txtMsg = new JTextArea(contenido);
        txtMsg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMsg.setEditable(false);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setOpaque(false);

        burbuja.add(lblEmisor, BorderLayout.NORTH);
        burbuja.add(txtMsg, BorderLayout.CENTER);

        // Set max width to 70% of container
        burbuja.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        // Wrapper for alignment
        JPanel wrapper = new JPanel(new FlowLayout(
                esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 4));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        wrapper.add(burbuja);

        pnlBurbujas.add(wrapper);
    }

    private void enviarMensaje() {
        String contenido = txtMensaje.getText().trim();
        if (contenido.isEmpty()) return;

        String target = (String) cmbDestinatario.getSelectedItem();
        String finalTarget = "— Todos —".equals(target) ? null : target;

        if (enviarListener != null) {
            enviarListener.onEnviarMensaje(finalTarget, contenido);
        }
        txtMensaje.setText("");
    }

    public void setEnviarListener(EnviarMensajeListener listener) {
        this.enviarListener = listener;
    }
}

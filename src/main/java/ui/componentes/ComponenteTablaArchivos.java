package ui.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class ComponenteTablaArchivos extends JPanel {

    private static final Color COLOR_FONDO       = new Color(245, 247, 250);
    private static final Color COLOR_TARJETA     = Color.WHITE;
    private static final Color COLOR_BORDE       = new Color(200, 210, 225);
    private static final Color COLOR_NOMBRE      = new Color(30,  40,  70);
    private static final Color COLOR_BADGE_FONDO = new Color(235, 240, 250);
    private static final Color COLOR_BADGE_TEXTO = new Color(60,  80, 140);
    private static final Color COLOR_BTN_FONDO   = new Color(240, 242, 246);
    private static final Color COLOR_BTN_BORDE   = new Color(190, 200, 218);
    private static final Color COLOR_BTN_TEXTO   = new Color(50,  65, 100);

    private static final Font FUENTE_NOMBRE = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FUENTE_BADGE  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FUENTE_BTN    = new Font("Segoe UI", Font.PLAIN, 12);

    public interface ArchivoActionListener {
        void onDescargar(String archivoId);
        void onVerComentarios(String archivoId, ComponenteComentario panelComentarios);
    }

    private final JPanel contenedor;
    private ArchivoActionListener listener;

    public ComponenteTablaArchivos() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(COLOR_FONDO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBackground(COLOR_FONDO);
        scroll.getViewport().setBackground(COLOR_FONDO);

        add(scroll, BorderLayout.CENTER);
    }

    public void setArchivoActionListener(ArchivoActionListener listener) {
        this.listener = listener;
    }

    public void updateFiles(List<Map<String, Object>> documentos) {
        contenedor.removeAll();

        if (documentos == null || documentos.isEmpty()) {
            JLabel vacio = new JLabel("No hay archivos disponibles.");
            vacio.setForeground(new Color(150, 160, 180));
            vacio.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenedor.add(Box.createVerticalStrut(30));
            contenedor.add(vacio);
        } else {
            for (Map<String, Object> doc : documentos) {
                contenedor.add(crearTarjeta(doc));
                contenedor.add(Box.createVerticalStrut(8));
            }
        }

        contenedor.revalidate();
        contenedor.repaint();
    }

    private JPanel crearTarjeta(Map<String, Object> doc) {
        String id          = doc.get("id")          != null ? doc.get("id").toString() : "";
        String nombre      = (String) doc.getOrDefault("nombre",      "Sin nombre");
        String extension   = (String) doc.getOrDefault("extension",   "—");
        String propietario = (String) doc.getOrDefault("propietario", "—");

        String tamanoStr = "0 B";
        Object sizeObj = doc.get("tamano_bytes");
        if (sizeObj != null) {
            try { tamanoStr = formatSize(Long.parseLong(sizeObj.toString())); }
            catch (Exception ignored) {}
        }

        // Tarjeta: GridBagLayout para control total de filas
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Fila 0: Nombre
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(FUENTE_NOMBRE);
        lblNombre.setForeground(COLOR_NOMBRE);
        tarjeta.add(lblNombre, gbc);

        // Fila 1: Badges
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel filaBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filaBadges.setBackground(COLOR_TARJETA);
        filaBadges.add(crearBadge(tamanoStr));
        filaBadges.add(crearBadge(extension));
        filaBadges.add(crearBadge(propietario));
        tarjeta.add(filaBadges, gbc);

        // Fila 2: Botón DESCARGAR
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        JButton btnDescargar = crearBoton("DESCARGAR");
        btnDescargar.addActionListener(e -> {
            if (listener != null) listener.onDescargar(id);
        });
        tarjeta.add(btnDescargar, gbc);

        // Panel comentarios oculto
        ComponenteComentario panelComentarios = new ComponenteComentario(id);
        panelComentarios.setVisible(false);

        // Fila 3: Botón VER COMENTARIOS
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnComentarios = crearBoton("VER COMENTARIOS");
        btnComentarios.addActionListener(e -> {
            boolean visible = !panelComentarios.isVisible();
            panelComentarios.setVisible(visible);
            btnComentarios.setText(visible ? "OCULTAR COMENTARIOS" : "VER COMENTARIOS");
            tarjeta.revalidate();
            tarjeta.repaint();
            contenedor.revalidate();
            contenedor.repaint();
        });
        tarjeta.add(btnComentarios, gbc);

        // Fila 4: Panel de comentarios (expandible)
        gbc.gridy = 4;
        gbc.insets = new Insets(6, 0, 0, 0);
        tarjeta.add(panelComentarios, gbc);

        return tarjeta;
    }

    private JLabel crearBadge(String texto) {
        JLabel badge = new JLabel(texto);
        badge.setFont(FUENTE_BADGE);
        badge.setForeground(COLOR_BADGE_TEXTO);
        badge.setBackground(COLOR_BADGE_FONDO);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)
        ));
        return badge;
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(220, 225, 235)
                        : COLOR_BTN_FONDO;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FUENTE_BTN);
        btn.setForeground(COLOR_BTN_TEXTO);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BTN_BORDE, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.setMinimumSize(new Dimension(50, 30));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return btn;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
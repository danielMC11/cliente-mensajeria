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

        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Fila 0: Nombre
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(FUENTE_NOMBRE);
        lblNombre.setForeground(COLOR_NOMBRE);
        tarjeta.add(lblNombre, gbc);

        // Fila 1: Badges
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel filaBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filaBadges.setBackground(COLOR_TARJETA);
        filaBadges.add(crearBadge(tamanoStr));
        filaBadges.add(crearBadge(extension));
        filaBadges.add(crearBadge(propietario));
        tarjeta.add(filaBadges, gbc);

        // Fila 2: Botón DESCARGAR → abre modal
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        JButton btnDescargar = crearBoton("DESCARGAR");
        btnDescargar.addActionListener(e -> abrirVentanaDescarga(id, nombre));
        tarjeta.add(btnDescargar, gbc);

        // Panel comentarios oculto
        ComponenteComentario panelComentarios = new ComponenteComentario(id);
        panelComentarios.setVisible(false);

        // Fila 3: Botón VER COMENTARIOS
        gbc.gridy  = 3;
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

        // Fila 4: Panel comentarios expandible
        gbc.gridy  = 4;
        gbc.insets = new Insets(6, 0, 0, 0);
        tarjeta.add(panelComentarios, gbc);

        return tarjeta;
    }

    // -----------------------------------------------------------------------
    // Ventana modal de descarga
    // -----------------------------------------------------------------------

    private void abrirVentanaDescarga(String archivoId, String nombreArchivo) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = (ventanaPadre instanceof Frame)
                ? new JDialog((Frame) ventanaPadre, "Descargar archivo", true)
                : new JDialog((Dialog) ventanaPadre, "Descargar archivo", true);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(Color.WHITE);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Título
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        JLabel lblTitulo = new JLabel("Descargar");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(30, 40, 70));
        contenido.add(lblTitulo, gbc);

        // Nombre del archivo
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel lblNombre = new JLabel(nombreArchivo);
        lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombre.setForeground(new Color(100, 115, 140));
        contenido.add(lblNombre, gbc);

        // Separador
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 228, 245));
        contenido.add(sep, gbc);

        // Botón Original
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        JButton btnOriginal = crearBotonModal("Descargar original", "Archivo tal como fue subido");
        btnOriginal.addActionListener(e -> {
            dialog.dispose();
            iniciarDescarga(archivoId, nombreArchivo, "ORG");
        });
        contenido.add(btnOriginal, gbc);

        // Botón Hash
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        JButton btnHash = crearBotonModal("Descargar hash", "Archivo de verificación de integridad");
        btnHash.addActionListener(e -> {
            dialog.dispose();
            iniciarDescarga(archivoId, nombreArchivo, "HSH");
        });
        contenido.add(btnHash, gbc);

        // Botón Encriptado
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        JButton btnEncriptado = crearBotonModal("Descargar encriptado", "Archivo cifrado con clave pública");
        btnEncriptado.addActionListener(e -> {
            dialog.dispose();
            iniciarDescarga(archivoId, nombreArchivo, "ENC");
        });
        contenido.add(btnEncriptado, gbc);

        // Cancelar
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setForeground(new Color(100, 115, 140));
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.setHorizontalAlignment(SwingConstants.CENTER);
        btnCancelar.addActionListener(e -> dialog.dispose());
        contenido.add(btnCancelar, gbc);

        dialog.setContentPane(contenido);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, 0));
        dialog.setLocationRelativeTo(ventanaPadre);
        dialog.setVisible(true);
    }

    /**
     * Replica exactamente la lógica de EditorGenerico#accion():
     * obtiene el Dashboard desde la jerarquía de ventanas y llama
     * dashboard.iniciarDescarga(docId, filename, format).
     */
    private void iniciarDescarga(String archivoId, String nombreArchivo, String format) {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana instanceof ui.Dashboard) {
            ((ui.Dashboard) ventana).iniciarDescarga(archivoId, nombreArchivo, format);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers visuales
    // -----------------------------------------------------------------------

    private JButton crearBotonModal(String titulo, String subtitulo) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(220, 228, 245)
                        : new Color(240, 244, 252);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new GridBagLayout());
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMinimumSize(new Dimension(280, 52));
        btn.setPreferredSize(new Dimension(280, 52));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx   = 0;
        g.weightx = 1.0;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.anchor  = GridBagConstraints.WEST;

        g.gridy = 0;
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(new Color(30, 50, 100));
        btn.add(lblTitulo, g);

        g.gridy = 1;
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(110, 125, 155));
        btn.add(lblSub, g);

        return btn;
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
        btn.setMinimumSize(new Dimension(50, 30));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        return btn;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
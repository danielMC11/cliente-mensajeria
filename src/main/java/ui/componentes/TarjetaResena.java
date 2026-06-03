package ui.componentes;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Tarjeta visual que representa una reseña individual.
 * El fondo cambia según el sentimiento:
 *   - POSITIVO → verde claro (estilo Steam thumbs-up)
 *   - NEGATIVO → rojo claro (estilo Steam thumbs-down)
 *   - PENDIENTE → gris claro (análisis en progreso)
 */
public class TarjetaResena extends JPanel {

    // Colores Steam-inspired
    private static final Color BG_POSITIVO = new Color(200, 240, 210);
    private static final Color BORDER_POSITIVO = new Color(76, 175, 80);
    private static final Color BG_NEGATIVO = new Color(255, 210, 210);
    private static final Color BORDER_NEGATIVO = new Color(244, 67, 54);
    private static final Color BG_PENDIENTE = new Color(245, 245, 245);
    private static final Color BORDER_PENDIENTE = new Color(189, 189, 189);

    private final String resenaId;
    private final JLabel lblSentimiento;
    private final JLabel lblConfianza;
    private String sentimiento = "PENDIENTE";

    public TarjetaResena(String resenaId, String autorUsername, String contenido) {
        this.resenaId = resenaId;
        setLayout(new BorderLayout(8, 4));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        aplicarEstilo("PENDIENTE", 0.0);

        // --- Header: avatar + username + sentiment badge ---
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        // Avatar (initial letter circle)
        String initial = autorUsername != null && !autorUsername.isEmpty()
                ? autorUsername.substring(0, 1).toUpperCase() : "?";
        JLabel lblAvatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 120, 160));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        lblAvatar.setPreferredSize(new Dimension(32, 32));
        lblAvatar.setMinimumSize(new Dimension(32, 32));

        JLabel lblUsuario = new JLabel(autorUsername != null ? autorUsername : "Anónimo");
        lblUsuario.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlUser.setOpaque(false);
        pnlUser.add(lblAvatar);
        pnlUser.add(lblUsuario);

        // Sentiment badge
        JPanel pnlBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBadge.setOpaque(false);

        lblSentimiento = new JLabel("⏳ Analizando...");
        lblSentimiento.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSentimiento.setOpaque(true);
        lblSentimiento.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        lblConfianza = new JLabel("");
        lblConfianza.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblConfianza.setForeground(new Color(120, 120, 120));

        pnlBadge.add(lblSentimiento);
        pnlBadge.add(lblConfianza);

        header.add(pnlUser, BorderLayout.WEST);
        header.add(pnlBadge, BorderLayout.EAST);

        // --- Content: review text ---
        JTextArea txtContenido = new JTextArea(contenido != null ? contenido : "");
        txtContenido.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtContenido.setEditable(false);
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setOpaque(false);
        txtContenido.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        add(header, BorderLayout.NORTH);
        add(txtContenido, BorderLayout.CENTER);
    }

    /**
     * Actualiza el sentimiento y colorea la tarjeta.
     * @param sentimiento "POSITIVO", "NEGATIVO", o "PENDIENTE"
     * @param confianza Porcentaje de confianza del análisis.
     */
    public void setSentimiento(String sentimiento, double confianza) {
        this.sentimiento = sentimiento;
        aplicarEstilo(sentimiento, confianza);
    }

    private void aplicarEstilo(String sentimiento, double confianza) {
        Color bg;
        Color borderColor;
        String badgeText;
        Color badgeBg;
        Color badgeFg;

        switch (sentimiento) {
            case "POSITIVO":
                bg = BG_POSITIVO;
                borderColor = BORDER_POSITIVO;
                badgeText = "👍 Positivo";
                badgeBg = BORDER_POSITIVO;
                badgeFg = Color.WHITE;
                break;
            case "NEGATIVO":
                bg = BG_NEGATIVO;
                borderColor = BORDER_NEGATIVO;
                badgeText = "👎 Negativo";
                badgeBg = BORDER_NEGATIVO;
                badgeFg = Color.WHITE;
                break;
            default:
                bg = BG_PENDIENTE;
                borderColor = BORDER_PENDIENTE;
                badgeText = "⏳ Analizando...";
                badgeBg = new Color(220, 220, 220);
                badgeFg = new Color(100, 100, 100);
                break;
        }

        setBackground(bg);
        setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        lblSentimiento.setText(badgeText);
        lblSentimiento.setBackground(badgeBg);
        lblSentimiento.setForeground(badgeFg);

        if (confianza > 0) {
            lblConfianza.setText(String.format("%.1f%%", confianza));
        } else {
            lblConfianza.setText("");
        }

        revalidate();
        repaint();
    }

    public String getResenaId() {
        return resenaId;
    }

    public String getSentimiento() {
        return sentimiento;
    }
}

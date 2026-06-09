package ui.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class ComponenteComentario extends JPanel {

    private static final Color COLOR_FONDO_PANEL    = new Color(248, 250, 255);
    private static final Color COLOR_BORDE          = new Color(200, 210, 225);
    private static final Color COLOR_ENCABEZADO_BG  = new Color(230, 237, 255);
    private static final Color COLOR_ENCABEZADO_FG  = new Color(35,  55, 120);
    private static final Color COLOR_USUARIO_FG     = new Color(50,  80, 160);
    private static final Color COLOR_TEXTO_FG       = new Color(40,  50,  70);
    private static final Color COLOR_SEPARADOR      = new Color(220, 228, 245);
    private static final Color COLOR_BTN            = new Color(50, 110, 220);
    private static final Color COLOR_BTN_TEXTO      = Color.WHITE;
    private static final Color COLOR_INPUT_BG       = Color.WHITE;
    private static final Color COLOR_INPUT_BORDE    = new Color(180, 195, 225);

    private static final Color COLOR_POSITIVO       = new Color(34, 139, 34);
    private static final Color COLOR_NEGATIVO       = new Color(200, 50, 50);
    private static final Color COLOR_NEUTRO         = new Color(130, 130, 130);
    private static final Color COLOR_SENTIMENT_BG   = new Color(240, 244, 255);

    private static final Font FUENTE_ENCABEZADO = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FUENTE_USUARIO    = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FUENTE_COMENTARIO = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FUENTE_SENTIMENT  = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FUENTE_CONFIDENCE = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FUENTE_BTN        = new Font("Segoe UI", Font.BOLD,  12);

    private final String archivoId;
    private final JPanel listaPanel;
    private final JTextArea areaTexto;
    private ComentarListener comentarListener;

    public interface ComentarListener {
        void onComentar(String archivoId, String texto);
    }

    public ComponenteComentario(String archivoId) {
        this.archivoId = archivoId;
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_FONDO_PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JLabel encabezado = new JLabel("  COMENTARIOS");
        encabezado.setFont(FUENTE_ENCABEZADO);
        encabezado.setForeground(COLOR_ENCABEZADO_FG);
        encabezado.setBackground(COLOR_ENCABEZADO_BG);
        encabezado.setOpaque(true);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        add(encabezado, BorderLayout.NORTH);

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(COLOR_FONDO_PANEL);

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 200));
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setBackground(COLOR_FONDO_PANEL);
        scroll.getViewport().setBackground(COLOR_FONDO_PANEL);
        add(scroll, BorderLayout.CENTER);

        JPanel panelEscritura = new JPanel(new BorderLayout(6, 0));
        panelEscritura.setBackground(COLOR_FONDO_PANEL);
        panelEscritura.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        areaTexto = new JTextArea(3, 1);
        areaTexto.setFont(FUENTE_COMENTARIO);
        areaTexto.setForeground(COLOR_TEXTO_FG);
        areaTexto.setBackground(COLOR_INPUT_BG);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDE, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        String placeholder = "Escribe un comentario...";
        areaTexto.setText(placeholder);
        areaTexto.setForeground(new Color(160, 170, 190));
        areaTexto.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (areaTexto.getText().equals(placeholder)) {
                    areaTexto.setText("");
                    areaTexto.setForeground(COLOR_TEXTO_FG);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (areaTexto.getText().isBlank()) {
                    areaTexto.setText(placeholder);
                    areaTexto.setForeground(new Color(160, 170, 190));
                }
            }
        });

        JButton btnComentar = crearBotonComentar();
        btnComentar.addActionListener(e -> enviarComentario(placeholder));

        panelEscritura.add(new JScrollPane(areaTexto), BorderLayout.CENTER);
        panelEscritura.add(btnComentar, BorderLayout.EAST);
        add(panelEscritura, BorderLayout.SOUTH);
    }

    public void setComentarListener(ComentarListener listener) {
        this.comentarListener = listener;
    }

    public void cargarComentarios(List<Map<String, Object>> comentarios) {
        listaPanel.removeAll();

        if (comentarios == null || comentarios.isEmpty()) {
            JLabel vacio = new JLabel("  Aún no hay comentarios.");
            vacio.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            vacio.setForeground(new Color(160, 170, 190));
            vacio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            listaPanel.add(vacio);
        } else {
            for (int i = 0; i < comentarios.size(); i++) {
                listaPanel.add(crearFilaComentario(comentarios.get(i)));
                if (i < comentarios.size() - 1) listaPanel.add(crearSeparador());
            }
        }

        listaPanel.add(Box.createVerticalGlue());
        listaPanel.revalidate();
        listaPanel.repaint();
    }

    public void agregarComentario(Map<String, Object> comentario) {
        if (listaPanel.getComponentCount() > 0) {
            Component last = listaPanel.getComponent(listaPanel.getComponentCount() - 1);
            if (last instanceof Box.Filler) listaPanel.remove(last);

            if (listaPanel.getComponentCount() == 1 && listaPanel.getComponent(0) instanceof JLabel) {
                listaPanel.removeAll();
            } else if (listaPanel.getComponentCount() > 0) {
                listaPanel.add(crearSeparador());
            }
        }

        listaPanel.add(crearFilaComentario(comentario));
        listaPanel.add(Box.createVerticalGlue());
        listaPanel.revalidate();
        listaPanel.repaint();
    }

    public String getArchivoId() { return archivoId; }

    private JPanel crearFilaComentario(Map<String, Object> comentario) {
        String usuario   = comentario.get("username")   != null ? comentario.get("username").toString()   : "Usuario";
        String texto     = comentario.get("content")    != null ? comentario.get("content").toString()    : "";
        String fecha     = comentario.get("created_at") != null ? "  ·  " + comentario.get("created_at") : "";
        String sentiment = comentario.get("sentiment")  != null ? comentario.get("sentiment").toString()  : null;
        Object confObj   = comentario.get("confidence");

        JPanel fila = new JPanel();
        fila.setLayout(new BoxLayout(fila, BoxLayout.Y_AXIS));
        fila.setBackground(COLOR_FONDO_PANEL);
        fila.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lblUsuario = new JLabel(usuario + fecha);
        lblUsuario.setFont(FUENTE_USUARIO);
        lblUsuario.setForeground(COLOR_USUARIO_FG);
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.add(lblUsuario);
        fila.add(Box.createVerticalStrut(3));

        JTextArea txtComentario = new JTextArea(texto);
        txtComentario.setFont(FUENTE_COMENTARIO);
        txtComentario.setForeground(COLOR_TEXTO_FG);
        txtComentario.setBackground(COLOR_FONDO_PANEL);
        txtComentario.setLineWrap(true);
        txtComentario.setWrapStyleWord(true);
        txtComentario.setEditable(false);
        txtComentario.setFocusable(false);
        txtComentario.setBorder(BorderFactory.createEmptyBorder());
        txtComentario.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtComentario.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        fila.add(txtComentario);

        if (sentiment != null || confObj != null) {
            fila.add(Box.createVerticalStrut(6));
            fila.add(crearFilaSentiment(sentiment, confObj));
        }

        return fila;
    }

    private JPanel crearFilaSentiment(String sentiment, Object confObj) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setBackground(COLOR_FONDO_PANEL);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        if (sentiment != null) {
            Color color;
            switch (sentiment.toUpperCase()) {
                case "POSITIVE":
                case "POSITIVO": color = COLOR_POSITIVO; break;
                case "NEGATIVE":
                case "NEGATIVO": color = COLOR_NEGATIVO; break;
                default:         color = COLOR_NEUTRO;   break;
            }

            JLabel badgeSentiment = new JLabel(sentiment.toUpperCase());
            badgeSentiment.setFont(FUENTE_SENTIMENT);
            badgeSentiment.setForeground(color);
            badgeSentiment.setBackground(COLOR_SENTIMENT_BG);
            badgeSentiment.setOpaque(true);
            badgeSentiment.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.brighter(), 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
            fila.add(badgeSentiment);
        }

        if (confObj != null) {
            try {
                double conf = Double.parseDouble(confObj.toString());
                int pct = (int) Math.round(conf);

                JLabel lblConf = new JLabel("Confidence:");
                lblConf.setFont(FUENTE_CONFIDENCE);
                lblConf.setForeground(COLOR_NEUTRO);
                fila.add(lblConf);

                fila.add(crearBarraConfianza(pct));

                // Modificado para mostrar 3 decimales
                String pctTexto = String.format(java.util.Locale.US, "%.3f%%", conf);
                JLabel lblPct = new JLabel(pctTexto);
                lblPct.setFont(FUENTE_SENTIMENT);
                lblPct.setForeground(COLOR_TEXTO_FG);
                fila.add(lblPct);
            } catch (Exception ignored) {}
        }

        return fila;
    }

    private JPanel crearBarraConfianza(int porcentaje) {
        return new JPanel() {
            { setPreferredSize(new Dimension(80, 10)); setBackground(COLOR_FONDO_PANEL); }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(210, 218, 235));
                g2.fillRoundRect(0, 0, w, h, h, h);

                int fill = (int) (w * porcentaje / 100.0);
                Color barColor = porcentaje >= 70 ? COLOR_POSITIVO
                        : porcentaje >= 40 ? new Color(200, 150, 30)
                          : COLOR_NEGATIVO;
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fill, h, h, h);

                g2.dispose();
            }
        };
    }

    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(COLOR_SEPARADOR);
        sep.setBackground(COLOR_SEPARADOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JButton crearBotonComentar() {
        JButton btn = new JButton("COMENTAR") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? COLOR_BTN.darker() : COLOR_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FUENTE_BTN);
        btn.setForeground(COLOR_BTN_TEXTO);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setMinimumSize(new Dimension(100, 34));
        btn.setMaximumSize(new Dimension(100, 34));
        return btn;
    }

    private void enviarComentario(String placeholder) {
        String texto = areaTexto.getText().trim();
        if (texto.isEmpty() || texto.equals(placeholder)) return;
        if (comentarListener != null) comentarListener.onComentar(archivoId, texto);
        areaTexto.setText(placeholder);
        areaTexto.setForeground(new Color(160, 170, 190));
    }
}
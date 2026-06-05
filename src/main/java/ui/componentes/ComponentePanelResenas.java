package ui.componentes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Panel que muestra las reseñas de un producto seleccionado.
 * Incluye cabecera con nombre del producto, lista de tarjetas de reseñas
 * y campo de entrada para publicar nuevas reseñas.
 */
public class ComponentePanelResenas extends JPanel {

    /** Listener para publicar reseñas. */
    public interface PublicarResenaListener {
        void onPublicarResena(String productoId, String contenido);
    }

    /** Listener para volver a la vista de productos. */
    public interface VolverListener {
        void onVolver();
    }

    private final JPanel pnlTarjetas;
    private final JLabel lblProductoNombre;
    private final JTextArea txtNuevaResena;
    private final JButton btnPublicar;
    private final JButton btnVolver;
    private final List<TarjetaResena> tarjetas = new ArrayList<>();

    private String productoIdActual;
    private PublicarResenaListener publicarListener;
    private VolverListener volverListener;

    public ComponentePanelResenas() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBorder(new EmptyBorder(4, 4, 8, 4));

        btnVolver = new JButton("← Volver a Productos");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            if (volverListener != null) volverListener.onVolver();
        });

        lblProductoNombre = new JLabel("Selecciona un producto");
        lblProductoNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblProductoNombre.setForeground(new Color(50, 50, 50));

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitulo.setOpaque(false);
        pnlTitulo.add(new JLabel("📦"));
        pnlTitulo.add(lblProductoNombre);

        header.add(btnVolver, BorderLayout.WEST);
        header.add(pnlTitulo, BorderLayout.CENTER);

        // --- Reviews list ---
        pnlTarjetas = new JPanel();
        pnlTarjetas.setLayout(new BoxLayout(pnlTarjetas, BoxLayout.Y_AXIS));
        pnlTarjetas.setBackground(new Color(250, 250, 252));

        JScrollPane scroll = new JScrollPane(pnlTarjetas);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // --- Input area ---
        JPanel pnlInput = new JPanel(new BorderLayout(8, 4));
        pnlInput.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lblEscribir = new JLabel("Escribe tu reseña:");
        lblEscribir.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtNuevaResena = new JTextArea(3, 40);
        txtNuevaResena.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtNuevaResena.setLineWrap(true);
        txtNuevaResena.setWrapStyleWord(true);
        txtNuevaResena.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        btnPublicar = new JButton("📝 Publicar Reseña");
        btnPublicar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPublicar.setBackground(new Color(52, 120, 246));
        btnPublicar.setForeground(Color.WHITE);
        btnPublicar.setFocusPainted(false);
        btnPublicar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPublicar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnPublicar.addActionListener(e -> publicarResena());

        JPanel pnlBottom = new JPanel(new BorderLayout(8, 0));
        pnlBottom.add(lblEscribir, BorderLayout.NORTH);
        pnlBottom.add(new JScrollPane(txtNuevaResena), BorderLayout.CENTER);
        pnlBottom.add(btnPublicar, BorderLayout.EAST);

        pnlInput.add(pnlBottom, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pnlInput, BorderLayout.SOUTH);
    }

    /** Configura el producto activo y limpia las reseñas. */
    public void setProductoActual(String productoId, String productoNombre) {
        this.productoIdActual = productoId;
        lblProductoNombre.setText("Reseñas de: " + productoNombre);
        tarjetas.clear();
        pnlTarjetas.removeAll();
        pnlTarjetas.revalidate();
        pnlTarjetas.repaint();
    }

    /** Actualiza la lista de reseñas desde los datos del servidor. */
    public void updateResenas(List<Map<String, Object>> resenas) {
        pnlTarjetas.removeAll();
        tarjetas.clear();

        if (resenas == null || resenas.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay reseñas aún. ¡Sé el primero en comentar!");
            lblVacio.setFont(new Font("SansSerif", Font.ITALIC, 13));
            lblVacio.setForeground(new Color(150, 150, 150));
            lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
            lblVacio.setBorder(new EmptyBorder(30, 0, 30, 0));
            pnlTarjetas.add(lblVacio);
        } else {
            for (Map<String, Object> r : resenas) {
                String id = r.get("id") != null ? r.get("id").toString()
                        : (r.get("document_id") != null ? r.get("document_id").toString() : "");
                String autor = (String) r.getOrDefault("propietario", "Anónimo");
                String contenido = (String) r.getOrDefault("contenido", "");

                TarjetaResena tarjeta = new TarjetaResena(id, autor, contenido);

                // Apply sentiment if already analyzed
                String sentimiento = (String) r.get("sentimiento");
                if (sentimiento != null && !sentimiento.isEmpty()) {
                    double confianza = 0.0;
                    Object confObj = r.get("confianza_porcentaje");
                    if (confObj instanceof Number) {
                        confianza = ((Number) confObj).doubleValue();
                    } else if (confObj instanceof String) {
                        try {
                            confianza = Double.parseDouble((String) confObj);
                        } catch (Exception ex) {}
                    }
                    String mapped = "Positivo".equalsIgnoreCase(sentimiento) ? "POSITIVO" : "NEGATIVO";
                    tarjeta.setSentimiento(mapped, confianza);
                }

                tarjetas.add(tarjeta);
                pnlTarjetas.add(tarjeta);
                pnlTarjetas.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        pnlTarjetas.revalidate();
        pnlTarjetas.repaint();
    }

    /** Finds a review card by ID and updates its sentiment colors. */
    public void actualizarSentimiento(String resenaId, String sentimiento, double confianza) {
        for (TarjetaResena tarjeta : tarjetas) {
            if (tarjeta.getResenaId().equals(resenaId)) {
                String mapped;
                if ("Positivo".equalsIgnoreCase(sentimiento)) {
                    mapped = "POSITIVO";
                } else if ("Negativo".equalsIgnoreCase(sentimiento)) {
                    mapped = "NEGATIVO";
                } else {
                    mapped = "PENDIENTE";
                }
                tarjeta.setSentimiento(mapped, confianza);
                break;
            }
        }
    }

    /** Adds a new pending review card at the top. */
    public TarjetaResena agregarResenaLocal(String tempId, String autorUsername, String contenido) {
        TarjetaResena tarjeta = new TarjetaResena(tempId, autorUsername, contenido);
        tarjetas.add(0, tarjeta);
        pnlTarjetas.add(tarjeta, 0);
        pnlTarjetas.add(Box.createRigidArea(new Dimension(0, 8)), 1);
        pnlTarjetas.revalidate();
        pnlTarjetas.repaint();

        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, pnlTarjetas);
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });

        return tarjeta;
    }

    private void publicarResena() {
        String contenido = txtNuevaResena.getText().trim();
        if (contenido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La reseña no puede estar vacía.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (productoIdActual == null || productoIdActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay un producto seleccionado.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (publicarListener != null) {
            publicarListener.onPublicarResena(productoIdActual, contenido);
        }
        txtNuevaResena.setText("");
    }

    public void setPublicarListener(PublicarResenaListener listener) {
        this.publicarListener = listener;
    }

    public void setVolverListener(VolverListener listener) {
        this.volverListener = listener;
    }

    public String getProductoIdActual() {
        return productoIdActual;
    }
}

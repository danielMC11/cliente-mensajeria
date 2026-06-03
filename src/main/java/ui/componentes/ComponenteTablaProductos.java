package ui.componentes;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Tabla de productos (archivos mapeados como entidad Producto).
 * Reemplaza al antiguo ComponenteTablaArchivos con un botón "Ver Reseñas".
 */
public class ComponenteTablaProductos extends JPanel {
    private DefaultTableModel modelo;
    private JTable tabla;

    /** Listener para cuando el usuario quiere ver las reseñas de un producto. */
    public interface ProductoResenasListener {
        void onVerResenas(String productoId, String productoNombre);
    }

    private ProductoResenasListener resenasListener;

    public ComponenteTablaProductos() {
        setLayout(new BorderLayout());
        // Columns: 0=ID(hidden), 1=Nombre(hidden, for download), 2=Nombre Producto, 3=Tamaño, 4=Extensión, 5=Propietario, 6=Ver Reseñas, 7=Descargar
        String[] columnas = { "ID", "NombreFile", "📦 Producto", "Tamaño", "Ext.", "Propietario", "Reseñas", "Descargar" };
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6 || c == 7; // Only Reseñas and Descargar are editable/clickable
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(50);
        tabla.setRowSelectionAllowed(false);
        tabla.setCellSelectionEnabled(false);
        tabla.setFocusable(false);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        TableColumnModel tcm = tabla.getColumnModel();

        // Hide ID column
        tcm.getColumn(0).setMinWidth(0);
        tcm.getColumn(0).setMaxWidth(0);
        tcm.getColumn(0).setPreferredWidth(0);

        // Hide NombreFile column (used internally for download)
        tcm.getColumn(1).setMinWidth(0);
        tcm.getColumn(1).setMaxWidth(0);
        tcm.getColumn(1).setPreferredWidth(0);

        // Producto name column
        tcm.getColumn(2).setPreferredWidth(220);

        // Tamaño
        tcm.getColumn(3).setPreferredWidth(90);

        // Extension
        tcm.getColumn(4).setPreferredWidth(60);

        // Propietario
        tcm.getColumn(5).setPreferredWidth(150);

        // Ver Reseñas button
        tcm.getColumn(6).setPreferredWidth(120);
        tcm.getColumn(6).setMinWidth(120);
        tcm.getColumn(6).setCellRenderer(new ButtonRenderer("💬 Reseñas", new Color(52, 120, 246)));
        tcm.getColumn(6).setCellEditor(new ButtonEditor("💬 Reseñas", tabla, row -> {
            String productoId = (String) tabla.getModel().getValueAt(row, 0);
            String productoNombre = (String) tabla.getModel().getValueAt(row, 2);
            if (resenasListener != null) {
                resenasListener.onVerResenas(productoId, productoNombre);
            }
        }));

        // Descargar (reuse RendererGenerico and EditorGenerico for download buttons)
        tcm.getColumn(7).setPreferredWidth(320);
        tcm.getColumn(7).setCellRenderer(new RendererGenerico(true));
        tcm.getColumn(7).setCellEditor(new EditorGenerico(tabla, true));

        // Alternate row colors for better readability
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return this;
            }
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public void setResenasListener(ProductoResenasListener listener) {
        this.resenasListener = listener;
    }

    public void updateProductos(List<Map<String, Object>> productos) {
        modelo.setRowCount(0);
        if (productos == null) return;
        for (Map<String, Object> doc : productos) {
            String nombre = (String) doc.get("nombre");
            String extension = (String) doc.get("extension");
            String propietario = (String) doc.get("propietario");

            String tamanoStr = "0 B";
            Object sizeObj = doc.get("tamano_bytes");
            if (sizeObj != null) {
                try {
                    long bytes = Long.parseLong(sizeObj.toString());
                    tamanoStr = formatSize(bytes);
                } catch (Exception e) { /* ignore */ }
            }

            String id = doc.get("id") != null ? doc.get("id").toString() : "";
            modelo.addRow(new Object[] { id, nombre, nombre, tamanoStr, extension, propietario, "", "" });
        }
    }

    public JTable getTabla() {
        return tabla;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }

    // --- Inner classes for button rendering in table cells ---

    /** Renders a styled button in a table cell. */
    private static class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton button;

        public ButtonRenderer(String text, Color bgColor) {
            setLayout(new GridBagLayout());
            setOpaque(true);
            button = new JButton(text);
            button.setFont(new Font("SansSerif", Font.BOLD, 11));
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(button);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250)));
            return this;
        }
    }

    /** Editor that fires an action when the button is clicked. */
    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton button;
        private final JTable tabla;
        private final java.util.function.IntConsumer onClick;

        public ButtonEditor(String text, JTable tabla, java.util.function.IntConsumer onClick) {
            this.tabla = tabla;
            this.onClick = onClick;
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            button = new JButton(text);
            button.setFont(new Font("SansSerif", Font.BOLD, 11));
            button.setBackground(new Color(52, 120, 246));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> {
                int row = tabla.getEditingRow();
                if (row >= 0 && onClick != null) {
                    onClick.accept(row);
                }
                fireEditingStopped();
            });
            panel.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}

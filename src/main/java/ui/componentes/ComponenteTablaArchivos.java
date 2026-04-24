package ui.componentes;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ComponenteTablaArchivos extends JPanel {
    private DefaultTableModel modelo;
    private JTable tabla;

    public ComponenteTablaArchivos() {
        setLayout(new BorderLayout());
        String[] columnas = { "ID", "Nombre", "Tamaño", "Extensión", "Propietario", "Descargar" };
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(45);
        tabla.setRowSelectionAllowed(false);
        tabla.setCellSelectionEnabled(false);
        tabla.setFocusable(false);

        // Ocultar ID
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);

        TableColumn colPropietario = tabla.getColumnModel().getColumn(4);
        colPropietario.setPreferredWidth(250);

        TableColumn col = tabla.getColumnModel().getColumn(5);
        col.setPreferredWidth(320);
        col.setCellRenderer(new RendererGenerico(true));
        col.setCellEditor(new EditorGenerico(tabla, true));

        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public void updateFiles(List<Map<String, Object>> documentos) {
        modelo.setRowCount(0);
        if (documentos == null)
            return;
        for (Map<String, Object> doc : documentos) {
            String nombre = (String) doc.get("nombre");
            String extension = (String) doc.get("extension");
            String propietario = (String) doc.get("propietario");

            // Formatear tamaño
            String tamanoStr = "0 B";
            Object sizeObj = doc.get("tamano_bytes");
            if (sizeObj != null) {
                try {
                    long bytes = Long.parseLong(sizeObj.toString());
                    tamanoStr = formatSize(bytes);
                } catch (Exception e) {
                }
            }

            String id = doc.get("id") != null ? doc.get("id").toString() : "";
            modelo.addRow(new Object[] { id, nombre, tamanoStr, extension, propietario, "" });
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
package ui.componentes;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ComponenteTablaMensajes extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;

    public ComponenteTablaMensajes() {
        setLayout(new BorderLayout());

        String[] col = { "Emisor", "Contenido", "Descargar" };
        modelo = new DefaultTableModel(col, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 1 || c == 2;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(90); // Altura generosa para varias líneas de texto
        tabla.setRowSelectionAllowed(false);
        tabla.setFillsViewportHeight(true);

        // --- MANEJO DE ANCHOS ---
        TableColumnModel tcm = tabla.getColumnModel();

        // Emisor: Ancho un poco más grande para que quepa la info
        tcm.getColumn(0).setPreferredWidth(180);
        tcm.getColumn(0).setMaxWidth(250);

        // Contenido: Flexible y amplio
        tcm.getColumn(1).setPreferredWidth(500);
        tcm.getColumn(1).setCellRenderer(new MensajeCopiableRenderer());
        tcm.getColumn(1).setCellEditor(new MensajeCopiableEditor());

        // Descargar: Ancho fijo para los dos botones
        tcm.getColumn(2).setPreferredWidth(220);
        tcm.getColumn(2).setMinWidth(220);
        tcm.getColumn(2).setCellRenderer(new RendererGenerico(false));
        tcm.getColumn(2).setCellEditor(new EditorGenerico(tabla, false));

        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public void updateMessages(java.util.List<java.util.Map<String, Object>> mensajes) {
        modelo.setRowCount(0);
        for (java.util.Map<String, Object> m : mensajes) {
            String emisor = (String) m.get("emisor");
            String contenido = (String) m.get("contenido");
            agregarMensaje(emisor, contenido);
        }
    }

    public void agregarMensaje(String emisor, String texto) {
        modelo.addRow(new Object[] { emisor, texto, "" });
    }
}
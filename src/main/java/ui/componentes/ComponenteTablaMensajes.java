package ui.componentes;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ComponenteTablaMensajes extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;

    public ComponenteTablaMensajes() {
        setLayout(new BorderLayout());

        String[] col = { "ID", "Nombre", "Emisor", "Contenido", "Analizar", "Descargar" };
        modelo = new DefaultTableModel(col, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3 || c == 4 || c == 5;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(90);
        tabla.setRowSelectionAllowed(false);
        tabla.setFillsViewportHeight(true);

        TableColumnModel tcm = tabla.getColumnModel();

        // Ocultar ID
        tcm.getColumn(0).setMinWidth(0);
        tcm.getColumn(0).setMaxWidth(0);
        tcm.getColumn(0).setPreferredWidth(0);

        // Ocultar Nombre (para que EditorGenerico lo encuentre en col 1)
        tcm.getColumn(1).setMinWidth(0);
        tcm.getColumn(1).setMaxWidth(0);
        tcm.getColumn(1).setPreferredWidth(0);

        // Emisor
        tcm.getColumn(2).setPreferredWidth(180);
        tcm.getColumn(2).setMaxWidth(250);

        // Contenido
        tcm.getColumn(3).setPreferredWidth(500);
        tcm.getColumn(3).setCellRenderer(new MensajeCopiableRenderer());
        tcm.getColumn(3).setCellEditor(new MensajeCopiableEditor());

        // Analizar
        tcm.getColumn(4).setPreferredWidth(120);
        tcm.getColumn(4).setMinWidth(120);
        tcm.getColumn(4).setCellRenderer(new RendererAnalisisMensaje());
        tcm.getColumn(4).setCellEditor(new EditorAnalisisMensaje(tabla));

        // Descargar
        tcm.getColumn(5).setPreferredWidth(220);
        tcm.getColumn(5).setMinWidth(220);
        tcm.getColumn(5).setCellRenderer(new RendererGenerico(false));
        tcm.getColumn(5).setCellEditor(new EditorGenerico(tabla, false));

        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private java.util.Map<String, String> analysisResults = new java.util.HashMap<>();

    public void setAnalysisResult(String messageId, String resultText) {
        analysisResults.put(messageId, resultText);
        for(int i = 0; i < modelo.getRowCount(); i++) {
            if(modelo.getValueAt(i, 0).equals(messageId)) {
                modelo.setValueAt(resultText, i, 4);
                break;
            }
        }
    }

    public void updateFiles(java.util.List<java.util.Map<String, Object>> mensajes) {
        modelo.setRowCount(0);
        if (mensajes == null)
            return;
        for (java.util.Map<String, Object> m : mensajes) {
            // Extraer ID de forma robusta (soporta 'id' o 'document_id')
            String id = "";
            if (m.get("id") != null) id = m.get("id").toString();
            else if (m.get("document_id") != null) id = m.get("document_id").toString();

            String nombre = m.get("nombre") != null ? m.get("nombre").toString() : "mensaje.txt";
            String propietario = (String) m.get("propietario");
            String contenido = (String) m.get("contenido");

            String analisisValue = analysisResults.getOrDefault(id, "");

            modelo.addRow(new Object[] { id, nombre, propietario, contenido, analisisValue, "" });
        }
    }
}
package ui.componentes;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


class EditorGenerico extends AbstractCellEditor implements TableCellEditor {
    private PanelBotonesGenerico panel;
    private JTable tabla;
    public EditorGenerico(JTable tabla, boolean mostrarO) {
        this.tabla = tabla;
        this.panel = new PanelBotonesGenerico(mostrarO);
        panel.btnOriginal.addActionListener(e -> { accion("Original"); fireEditingStopped(); });
        panel.btnHash.addActionListener(e -> { accion("Hash"); fireEditingStopped(); });
        panel.btnEnc.addActionListener(e -> { accion("Encriptado"); fireEditingStopped(); });
    }
    private void accion(String tipo) {
        int row = tabla.getEditingRow();
        String docId = (String) tabla.getValueAt(row, 0);
        String filename = (String) tabla.getValueAt(row, 1);
        
        String format = "";
        if ("Original".equals(tipo)) format = "ORG";
        else if ("Hash".equals(tipo)) format = "HSH";
        else if ("Encriptado".equals(tipo)) format = "ENC";

        ui.Dashboard dashboard = (ui.Dashboard) javax.swing.SwingUtilities.getWindowAncestor(tabla);
        if (dashboard != null) {
            dashboard.iniciarDescarga(docId, filename, format);
        }
    }
    @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
        panel.setBackground(t.getSelectionBackground());
        return panel;
    }
    @Override public Object getCellEditorValue() { return ""; }
}
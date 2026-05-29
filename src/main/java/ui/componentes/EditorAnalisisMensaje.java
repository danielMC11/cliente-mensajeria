package ui.componentes;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorAnalisisMensaje extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private JButton button;
    private JTable tabla;
    private String currentValue;

    public EditorAnalisisMensaje(JTable tabla) {
        this.tabla = tabla;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = (String) value;
        if (currentValue == null || currentValue.isEmpty()) {
            button.setText("Analizar");
            button.setEnabled(true);
        } else {
            button.setText(currentValue);
            button.setEnabled(false);
        }
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return currentValue;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentValue == null || currentValue.isEmpty()) {
            int row = tabla.getEditingRow();
            if (row >= 0) {
                String docId = (String) tabla.getValueAt(row, 0);
                String contenido = (String) tabla.getValueAt(row, 3);
                ui.Dashboard dashboard = (ui.Dashboard) javax.swing.SwingUtilities.getWindowAncestor(tabla);
                if (dashboard != null) {
                    dashboard.analizarMensaje(docId, contenido);
                    button.setText("...");
                    currentValue = "...";
                }
            }
        }
        fireEditingStopped();
    }
}

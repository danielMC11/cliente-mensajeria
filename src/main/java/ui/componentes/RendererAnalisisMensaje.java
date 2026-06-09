package ui.componentes;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RendererAnalisisMensaje extends JPanel implements TableCellRenderer {
    private final JButton btnAnalizar = new JButton("Analizar");
    private final JLabel lblResultado = new JLabel("", SwingConstants.CENTER);

    public RendererAnalisisMensaje() {
        setLayout(new GridBagLayout());
        setOpaque(true);

        btnAnalizar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAnalizar.setBackground(new Color(52, 120, 246));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFocusPainted(false);
        btnAnalizar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnAnalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResultado.setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        removeAll();
        String val = (String) value;
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        if (val == null || val.isEmpty()) {
            add(btnAnalizar);
        } else if ("Error".equals(val)) {
            lblResultado.setText("Error");
            lblResultado.setForeground(Color.GRAY);
            add(lblResultado);
        } else if (val.startsWith("Positivo")) {
            lblResultado.setText(val);
            lblResultado.setForeground(new Color(0, 150, 60));
            add(lblResultado);
        } else if (val.startsWith("No calificable") || val.startsWith("NO_CALIFICABLE")) {
            lblResultado.setText(val);
            lblResultado.setForeground(Color.GRAY);
            add(lblResultado);
        } else {
            lblResultado.setText(val);
            lblResultado.setForeground(new Color(200, 30, 30));
            add(lblResultado);
        }
        return this;
    }
}

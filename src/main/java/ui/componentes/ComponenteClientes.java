package ui.componentes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Panel que muestra la lista federada de clientes conectados a TODA la red P2P.
 *
 * Columnas: Usuario | Servidor | Tipo (LOCAL / REMOTO)
 *
 * Los clientes LOCALES se muestran en azul, los REMOTOS en naranja.
 * El botón "Refrescar" solicita LIST_CLIENTS al servidor.
 *
 * Requerimiento: "Cada servidor deberá actualizar la información de los clientes
 * disponibles, deben incluir los clientes y los clientes de otros servidores."
 */
public class ComponenteClientes extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnRefresh;

    /** Lista en memoria de los clientes actuales (para el dropdown de envío dirigido). */
    private final List<String> currentUsernames = new ArrayList<>();

    public ComponenteClientes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));
        setPreferredSize(new Dimension(240, 0));

        // Cabeceras de la tabla
        String[] columns = {"Usuario", "Servidor", "Tipo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFocusable(false);
        table.setRowHeight(22);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Colorear filas según tipo LOCAL/REMOTO
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String tipo = (String) tbl.getModel().getValueAt(row, 2);
                if (!isSelected) {
                    if ("LOCAL".equals(tipo)) {
                        setBackground(new Color(220, 235, 255));  // azul claro
                        setForeground(new Color(0, 60, 140));
                    } else {
                        setBackground(new Color(255, 237, 210));  // naranja claro
                        setForeground(new Color(140, 70, 0));
                    }
                }
                return this;
            }
        });

        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        btnRefresh = new JButton("⟳ Refrescar");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 11));

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setRefreshAction(java.awt.event.ActionListener al) {
        btnRefresh.addActionListener(al);
    }

    /**
     * Actualiza la tabla con la lista federada de clientes.
     * Cada cliente incluye: username, servidor, tipo (LOCAL/REMOTO).
     */
    public void updateClients(List<Map<String, Object>> clientes) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            currentUsernames.clear();
            int count = 0;
            for (Map<String, Object> c : clientes) {
                String username = (String) c.getOrDefault("username", "?");
                String servidor = (String) c.getOrDefault("servidor", "local");
                String tipo     = (String) c.getOrDefault("tipo", "LOCAL");
                tableModel.addRow(new Object[]{username, servidor, tipo});
                currentUsernames.add(username);
                count++;
            }
            setBorder(BorderFactory.createTitledBorder("Clientes (" + count + ")"));
        });
    }

    /**
     * Retorna los usernames actuales para el dropdown del diálogo de envío dirigido.
     */
    public List<String> getCurrentUsernames() {
        return new ArrayList<>(currentUsernames);
    }
}
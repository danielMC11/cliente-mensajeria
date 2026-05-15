package ui.componentes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel que muestra el estado de todos los servidores conocidos en la red P2P.
 *
 * Columnas: Node ID | Dirección | Estado | Heartbeat | Tipo
 *
 * Color por estado:
 *   ALIVE     → verde
 *   SUSPECTED → amarillo/naranja
 *   DOWN      → rojo
 *
 * Requerimiento: "Detección de servidores amigos: listar servidores conectados y desconectados."
 * Requerimiento: "Adicionar servicios para mostrar la información de otros servidores."
 */
public class ComponenteServidores extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnRefresh;

    public ComponenteServidores() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Servidores Peers"));

        String[] columns = {"Node ID", "Dirección", "Estado", "Heartbeat", "Tipo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFocusable(false);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Colorear filas según estado del servidor
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String estado = String.valueOf(tbl.getModel().getValueAt(row, 2));
                if (!isSelected) {
                    switch (estado) {
                        case "ALIVE":
                            setBackground(new Color(220, 255, 220));
                            setForeground(new Color(0, 100, 0));
                            break;
                        case "SUSPECTED":
                        case "JOINING":
                            setBackground(new Color(255, 250, 200));
                            setForeground(new Color(140, 100, 0));
                            break;
                        case "DOWN":
                            setBackground(new Color(255, 220, 220));
                            setForeground(new Color(140, 0, 0));
                            break;
                        default:
                            setBackground(table.getBackground());
                            setForeground(table.getForeground());
                    }
                }
                return this;
            }
        });

        // Anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        btnRefresh = new JButton("⟳ Refrescar Servidores");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 11));

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setRefreshAction(java.awt.event.ActionListener al) {
        btnRefresh.addActionListener(al);
    }

    /**
     * Actualiza la tabla con la lista de servidores conocidos.
     * Cada servidor tiene: nodeId, host, clusterPort, clientPort, estado, tipo.
     */
    public void updateServers(List<Map<String, Object>> servers) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            int alive = 0, down = 0;
            for (Map<String, Object> s : servers) {
                String nodeId      = str(s, "nodeId");
                String host        = str(s, "host");
                String clusterPort = str(s, "clusterPort");
                String estado      = str(s, "estado");
                String heartbeat   = formatHeartbeat(s.get("ultimoHeartbeatMs"));
                String tipo        = str(s, "tipo");

                tableModel.addRow(new Object[]{
                        nodeId,
                        host + ":" + clusterPort,
                        estado,
                        heartbeat,
                        tipo
                });
                if ("ALIVE".equals(estado)) alive++;
                else if ("DOWN".equals(estado)) down++;
            }
            setBorder(BorderFactory.createTitledBorder(
                    "Servidores Peers (" + servers.size() + " total | " + alive + " activos | " + down + " caídos)"));
        });
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "N/A";
    }

    private String formatHeartbeat(Object ms) {
        if (ms == null) return "LOCAL";
        try {
            long millis = Long.parseLong(ms.toString());
            if (millis < 1000) return millis + " ms";
            return (millis / 1000) + " s";
        } catch (NumberFormatException e) {
            return ms.toString();
        }
    }
}

package ui.componentes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel que muestra los logs consolidados de TODOS los servidores de la red.
 *
 * Agrupa las entradas por servidor (LOCAL / REMOTO con nodeId).
 * Columnas: Servidor | Logs
 *
 * Requerimiento: "Adicionar servicios para mostrar los logs de otros servidores."
 */
public class ComponentePeerLogs extends JPanel {

    private final JTextArea textArea;
    private final JButton btnRefresh;

    public ComponentePeerLogs() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Logs Remotos"));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setForeground(new Color(30, 30, 80));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        btnRefresh = new JButton("⟳ Refrescar Logs Remotos");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 11));

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setRefreshAction(java.awt.event.ActionListener al) {
        btnRefresh.addActionListener(al);
    }

    /**
     * Muestra los logs de todos los servidores.
     * Cada entrada de peerLogs tiene: nodeId, tipo (LOCAL/REMOTO), logs (JSON string).
     */
    public void updatePeerLogs(List<Map<String, Object>> peerLogs) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> entry : peerLogs) {
                String nodeId = entry.getOrDefault("nodeId", "?").toString();
                String tipo   = entry.getOrDefault("tipo", "REMOTO").toString();
                String logs   = entry.getOrDefault("logs", "{}").toString();

                sb.append("═════════════════════════════════════════════════════\n");
                sb.append("  SERVIDOR: ").append(nodeId)
                  .append("  [").append(tipo).append("]\n");
                sb.append("─────────────────────────────────────────────────────\n");
                sb.append(logs.replace(",", ",\n  ")).append("\n\n");
            }
            textArea.setText(sb.toString());
            textArea.setCaretPosition(0);
        });
    }
}

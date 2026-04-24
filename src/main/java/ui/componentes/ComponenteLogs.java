package ui.componentes;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ComponenteLogs extends JPanel {
    private JTextArea areaTexto;

    private JButton btnRefresh;

    public ComponenteLogs() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("INFORME LOGS"));
        setPreferredSize(new Dimension(320, 0));

        btnRefresh = new JButton("Refrescar Logs");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 10));

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 11)); // Fuente tipo consola
        areaTexto.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setRefreshAction(java.awt.event.ActionListener al) {
        btnRefresh.addActionListener(al);
    }



    public void limpiarLogs() {
        areaTexto.setText("");
    }

    public void updateLogs(java.util.List<java.util.Map<String, Object>> logs) {
        limpiarLogs();
        if (logs == null) return;
        for (java.util.Map<String, Object> log : logs) {
            try {
                String sender   = nullSafe(log.get("sender"));
                String action   = nullSafe(log.get("action"));
                String status   = nullSafe(log.get("status"));
                String details  = nullSafe(log.get("details"));
                String createdAt = log.get("timestamp") != null
                        ? log.get("timestamp").toString()
                        : (log.get("created_at") != null ? log.get("created_at").toString() : "--");

                String docId   = nullSafe(log.get("document_id"));
                String docPart = !docId.trim().isEmpty() ? " [Doc: " + docId + "]" : "";

                String logLine = String.format("[%s] %s: %s%s (%s) - %s",
                        createdAt, sender, action, docPart, status, details);
                areaTexto.append(logLine + "\n");
            } catch (Exception ex) {
                areaTexto.append("[LOG ERROR] " + ex.getMessage() + "\n");
            }
        }
        SwingUtilities.invokeLater(() -> {
            areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
        });
    }

    private String nullSafe(Object o) {
        return o != null ? o.toString() : "";
    }

    public void registrarEvento(String mensaje) {
        // Obtenemos la hora actual para cada log
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        areaTexto.append("[" + hora + "] " + mensaje + "\n");

        // Asegura que el scroll baje automáticamente al final
        SwingUtilities.invokeLater(() -> {
            areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
        });
    }
}
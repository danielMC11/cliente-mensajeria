package ui.componentes;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ComponenteClientes extends JPanel {
    private DefaultListModel<String> modeloLista;
    private JList<String> lista;

    private JButton btnRefresh;

    public ComponenteClientes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));
        setPreferredSize(new Dimension(220, 0));

        btnRefresh = new JButton("Refrescar Clientes");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 10));

        modeloLista = new DefaultListModel<>();
        lista = new JList<>(modeloLista);
        lista.setSelectionBackground(lista.getBackground());
        lista.setSelectionForeground(lista.getForeground());
        lista.setFocusable(false);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setRefreshAction(java.awt.event.ActionListener al) {
        btnRefresh.addActionListener(al);
    }


    public void updateClients(List<Map<String, Object>> clientes) {
        modeloLista.clear();
        setBorder(BorderFactory.createTitledBorder("Clientes Conectados (" + clientes.size() + ")"));
        for (Map<String, Object> cliente : clientes) {
            String username = (String) cliente.get("username");
            String ip = (String) cliente.get("ip");
            String fecha = (String) cliente.get("fecha_inicio");
            
            modeloLista.addElement("<html><b>" + username + "</b><br/>IP: " + ip + "<br/><font color='gray'>Desde: " + fecha + "</font></html>");
        }
    }
}
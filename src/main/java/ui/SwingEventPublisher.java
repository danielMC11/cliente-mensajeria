package ui;

import domain.ports.UIEventPublisher;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class SwingEventPublisher implements UIEventPublisher {
    private Dashboard dashboard;

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void onClientsUpdated(List<Map<String, Object>> clients) {
        if (dashboard != null && clients != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelClientes().updateClients(clients);
                } catch (Exception ex) {
                    System.err.println("Error actualizando clientes: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onLogsUpdated(List<Map<String, Object>> logs) {
        if (dashboard != null && logs != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelLogs().updateLogs(logs);
                } catch (Exception ex) {
                    System.err.println("Error actualizando logs: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onMessagesUpdated(List<Map<String, Object>> messages) {
        if (dashboard != null && messages != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getTablaMensajes().updateFiles(messages);
                } catch (Exception ex) {
                    System.err.println("Error actualizando mensajes: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onDocumentsUpdated(List<Map<String, Object>> documents) {
        if (dashboard != null && documents != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getTablaArchivos().updateFiles(documents);
                } catch (Exception ex) {
                    System.err.println("Error actualizando documentos: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onUploadStatus(boolean success, String message) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(dashboard, message, "Estado de Subida",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    @Override
    public void onDownloadFinished(boolean success, String filename) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() -> {
                String msg = success ? "Descarga exitosa: " + filename : "Error en la descarga de: " + filename;
                JOptionPane.showMessageDialog(dashboard, msg, "Estado de Descarga",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    @Override
    public void onConnectAck(String status, String message) {
        System.out.println("Conexión confirmada por el servidor: " + status + " - " + message);
    }

    @Override
    public void onUploadInitAck(String token) {
        if (dashboard != null && dashboard.getTcpClient() != null) {
            dashboard.getTcpClient().startFileTransfer(token);
        }
    }

    @Override
    public void onDownloadInitAck(String token, long size) {
        if (dashboard != null && dashboard.getTcpClient() != null) {
            dashboard.getTcpClient().startDownloadTransfer(token, size);
        }
    }
}

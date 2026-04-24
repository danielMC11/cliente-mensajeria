package config;

import ui.Dashboard;
import java.util.List;
import java.util.Map;

public class ClientRouter {
    private Dashboard dashboard;

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public void route(String json) {
        if (json == null || json.trim().isEmpty())
            return;
        System.out.println("Router recibió: " + json);
        try {
            Map map = JSONSerializer.deserialize(json, Map.class);
            String action = (String) map.get("action");
            if (action == null) {
                if (map.containsKey("status")) {
                    String status = (String) map.get("status");
                    if ("UPLOAD_SUCCESS".equals(status) || "UPLOAD_FAILED".equals(status)) {
                        System.out.println("Server responded: " + status);
                        if (dashboard != null) {
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                String msg = "UPLOAD_SUCCESS".equals(status) ? "Archivo subido con éxito"
                                        : "Error al subir el archivo";
                                javax.swing.JOptionPane.showMessageDialog(dashboard, msg, "Estado de Subida",
                                        "UPLOAD_SUCCESS".equals(status) ? javax.swing.JOptionPane.INFORMATION_MESSAGE
                                                : javax.swing.JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                }
                return;
            }

            if (action.equals("CONNECT_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null) {
                    String status = (String) payload.get("status");
                    String message = (String) payload.get("message");
                    System.out.println("Conexión confirmada por el servidor: " + status + " - " + message);
                }
            } else if (action.equals("LIST_CLIENTS_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && payload.containsKey("clientes")) {
                    List<Map<String, Object>> clientes = (List<Map<String, Object>>) payload.get("clientes");
                    if (dashboard != null && clientes != null) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                dashboard.getPanelClientes().updateClients(clientes);
                            } catch (Exception ex) {
                                System.err.println("Error actualizando clientes: " + ex.getMessage());
                            }
                        });
                    }
                }
            } else if (action.equals("LIST_LOGS_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && payload.containsKey("logs")) {
                    List<Map<String, Object>> logs = (List<Map<String, Object>>) payload.get("logs");
                    if (dashboard != null) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                dashboard.getPanelLogs().updateLogs(logs);
                            } catch (Exception ex) {
                                System.err.println("Error actualizando logs: " + ex.getMessage());
                            }
                        });
                    }
                }
            } else if (action.equals("LIST_MESSAGES_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && payload.containsKey("mensajes")) {
                    List<Map<String, Object>> mensajes = (List<Map<String, Object>>) payload.get("mensajes");
                    if (dashboard != null) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                dashboard.getTablaMensajes().updateFiles(mensajes);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                }
            } else if (action.equals("LIST_DOCUMENTS_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && payload.containsKey("documentos")) {
                    List<Map<String, Object>> documentos = (List<Map<String, Object>>) payload.get("documentos");
                    if (dashboard != null) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                dashboard.getTablaArchivos().updateFiles(documentos);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                }
            } else if (action.equals("UPLOAD_INIT_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && "SUCCESS".equals(payload.get("status"))) {
                    String token = (String) payload.get("message");
                    if (dashboard != null) {
                        dashboard.getTcpClient().startFileTransfer(token);
                    }
                }
            } else if (action.equals("DOWNLOAD_INIT_ACK")) {
                Map payload = (Map) map.get("payload");
                if (payload != null && "SUCCESS".equals(payload.get("status"))) {
                    String token = (String) payload.get("message");
                    long size = 0;
                    try {
                        Object sizeObj = payload.get("size_bytes");
                        if (sizeObj != null) {
                            // Usar Double para evitar errores si viene como "123.0"
                            size = Double.valueOf(sizeObj.toString()).longValue();
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing size_bytes: " + e.getMessage());
                    }

                    if (dashboard != null) {
                        dashboard.getTcpClient().startDownloadTransfer(token, size);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error ruteando mensaje: " + json);
            e.printStackTrace();
        }
    }

    public void notifyDownloadResult(boolean success, String filename) {
        if (dashboard != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                String msg = success ? "Descarga exitosa: " + filename : "Error en la descarga de: " + filename;
                javax.swing.JOptionPane.showMessageDialog(dashboard, msg, "Estado de Descarga",
                        success ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}

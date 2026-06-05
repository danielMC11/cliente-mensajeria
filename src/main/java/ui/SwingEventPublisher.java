package ui;

import domain.ports.UIEventPublisher;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Implementación Swing de UIEventPublisher.
 * Despacha todos los eventos al Event Dispatch Thread (EDT) de Swing.
 *
 * Extensión P2P: implementa los eventos de cluster (join/leave de servidores,
 * lista de peers, logs remotos).
 */
public class SwingEventPublisher implements UIEventPublisher {
    private Dashboard dashboard;
    private final java.util.Map<String, ProgressMonitor> progressMonitors = new java.util.concurrent.ConcurrentHashMap<>();

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    // ── Eventos existentes ────────────────────────────────────────────────────

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
                    dashboard.getPanelChat().updateMessages(messages);
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
                    dashboard.getPanelProductos().updateProductos(documents);
                } catch (Exception ex) {
                    System.err.println("Error actualizando documentos: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onUploadStatus(boolean success, String message, String targetUsername) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(dashboard, message + " " + targetUsername, "Estado de Subida",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
            );
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
        if (dashboard != null) {
            if (dashboard.getTcpClient() != null) {
                dashboard.getTcpClient().startFileTransfer(token);
            } else if (dashboard.getUdpClient() != null) {
                dashboard.getUdpClient().startFileTransfer(token);
            }
        }
    }

    @Override
    public void onDownloadInitAck(String token, long size) {
        if (dashboard != null) {
            if (dashboard.getTcpClient() != null) {
                dashboard.getTcpClient().startDownloadTransfer(token, size);
            } else if (dashboard.getUdpClient() != null) {
                dashboard.getUdpClient().startDownloadTransfer(token, size);
            }
        }
    }

    @Override
    public void onDownloadProgress(String filename, long currentBytes, long totalBytes) {
        SwingUtilities.invokeLater(() -> {
            ProgressMonitor pm = progressMonitors.get(filename);
            if (pm == null) {
                pm = new ProgressMonitor(dashboard, "Descargando " + filename, "Iniciando...", 0, 100);
                pm.setMillisToDecideToPopup(100);
                pm.setMillisToPopup(100);
                progressMonitors.put(filename, pm);
            }
            
            int percent = (int) ((currentBytes * 100) / totalBytes);
            pm.setProgress(percent);
            pm.setNote("Completado: " + percent + "% (" + (currentBytes / 1024) + " KB / " + (totalBytes / 1024) + " KB)");
            
            if (currentBytes >= totalBytes || pm.isCanceled()) {
                pm.close();
                progressMonitors.remove(filename);
            }
        });
    }



    // ── Mensaje entrante en tiempo real ───────────────────────────────────────

    /**
     * Muestra un mensaje recibido (privado o broadcast federado) en un diálogo
     * emergente para que el destinatario lo vea inmediatamente.
     * También refresca la pestaña de mensajes históricos.
     */
    @Override
    public void onNewMessage(String message) {
        if (dashboard == null) return;
        // Refrescar la tabla de mensajes silenciosamente (sin popup)
        // El mensaje aparecerá en la tabla gracias al filtro por usuario en LIST_MESSAGES
        SwingUtilities.invokeLater(() -> {
            if (dashboard.getTcpClient() != null || dashboard.getUdpClient() != null) {
                dashboard.enviarPeticion("LIST_MESSAGES");
            }
        });
    }

    @Override
    public void onMessageAnalyzed(String status, String sentimiento, double confianza) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() -> {
                String id = dashboard.getPendingAnalyzeId();
                if (id != null) {
                    dashboard.getPanelResenas().actualizarSentimiento(id, sentimiento, confianza);
                    dashboard.setPendingAnalyzeId(null);
                }
            });
        }
    }

    @Override
    public void onSendMessageAck(String status, String message) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() -> {
                dashboard.showSendMessageAck(status, message);
            });
        }
    }

    @Override
    public void onServerDisconnected() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    dashboard,
                    "<html><b>⚠ Conexión perdida</b><br><br>"
                    + "El servidor ha cerrado la conexión.<br>"
                    + "Por favor, verifica que el servidor esté activo<br>"
                    + "y vuelve a iniciar el cliente.</html>",
                    "Servidor desconectado",
                    JOptionPane.ERROR_MESSAGE
            );
            if (dashboard != null) {
                dashboard.dispose();
            }
        });
    }

    // ── Eventos P2P distribuidos ──────────────────────────────────────────────

    /**
     * Un servidor se unió a la red. Muestra una notificación discreta en la barra de estado
     * y actualiza el panel de servidores si está visible.
     */
    @Override
    public void onServerJoined(Map<String, Object> serverInfo) {
        if (dashboard == null) return;
        String nodeId = str(serverInfo, "nodeId");
        String host   = str(serverInfo, "host");
        SwingUtilities.invokeLater(() -> {
            dashboard.showClusterNotification(
                    "🟢 Servidor unido: " + nodeId + " (" + host + ")",
                    new Color(0, 120, 0));
            // Solicitar lista actualizada de servidores
            dashboard.enviarPeticion("LIST_PEER_INFO");
        });
    }

    /**
     * Un servidor se desconectó o es sospechoso. Notificación en rojo/amarillo.
     */
    @Override
    public void onServerLeft(Map<String, Object> serverInfo) {
        if (dashboard == null) return;
        String nodeId    = str(serverInfo, "nodeId");
        String eventType = str(serverInfo, "eventType");
        boolean isSuspected = "SERVER_SUSPECTED".equals(eventType);
        SwingUtilities.invokeLater(() -> {
            String msg   = isSuspected
                    ? "🟡 Servidor sospechoso: " + nodeId
                    : "🔴 Servidor desconectado: " + nodeId;
            Color color  = isSuspected ? new Color(160, 100, 0) : new Color(160, 0, 0);
            dashboard.showClusterNotification(msg, color);
            // Solicitar lista actualizada de servidores
            dashboard.enviarPeticion("LIST_PEER_INFO");
        });
    }

    /**
     * Lista actualizada de todos los servidores conocidos → actualiza ComponenteServidores.
     */
    @Override
    public void onPeerInfoUpdated(List<Map<String, Object>> servers) {
        if (dashboard != null && servers != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelServidores().updateServers(servers);
                } catch (Exception ex) {
                    System.err.println("Error actualizando servidores: " + ex.getMessage());
                }
            });
        }
    }

    /**
     * Logs consolidados de todos los servidores → actualiza ComponentePeerLogs.
     */
    @Override
    public void onPeerLogsReceived(List<Map<String, Object>> peerLogs) {
        if (dashboard != null && peerLogs != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelPeerLogs().updatePeerLogs(peerLogs);
                } catch (Exception ex) {
                    System.err.println("Error actualizando logs remotos: " + ex.getMessage());
                }
            });
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "N/A";
    }

    // ── Eventos de Productos y Reseñas ────────────────────────────────────────

    @Override
    public void onProductosActualizados(List<Map<String, Object>> productos) {
        if (dashboard != null && productos != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelProductos().updateProductos(productos);
                } catch (Exception ex) {
                    System.err.println("Error actualizando productos: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onResenasActualizadas(List<Map<String, Object>> resenas) {
        if (dashboard != null && resenas != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboard.getPanelResenas().updateResenas(resenas);
                } catch (Exception ex) {
                    System.err.println("Error actualizando reseñas: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void onResenaAnalizada(String resenaId, String sentimiento, double confianza) {
        if (dashboard != null) {
            SwingUtilities.invokeLater(() -> {
                dashboard.getPanelResenas().actualizarSentimiento(resenaId, sentimiento, confianza);
            });
        }
    }
}

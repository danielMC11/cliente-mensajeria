package domain.ports;

import java.util.List;
import java.util.Map;

/**
 * Puerto de publicación de eventos hacia la capa UI.
 * Extiende con eventos distribuidos P2P: join/leave de servidores,
 * lista federada de peers y logs remotos.
 */
public interface UIEventPublisher {
    // --- Eventos existentes ---
    void onClientsUpdated(List<Map<String, Object>> clients);
    void onLogsUpdated(List<Map<String, Object>> logs);
    void onMessagesUpdated(List<Map<String, Object>> messages);
    void onDocumentsUpdated(List<Map<String, Object>> documents);
    void onUploadStatus(boolean success, String message);
    void onDownloadFinished(boolean success, String filename);
    void onConnectAck(String status, String message);
    void onUploadInitAck(String token);
    void onDownloadInitAck(String token, long size);
    void onDownloadProgress(String filename, long currentBytes, long totalBytes);

    /** Mensaje de texto recibido en tiempo real (privado o broadcast federado). */
    void onNewMessage(String message);
    
    void onMessageAnalyzed(String status, String sentimiento, double confianza);

    /** El servidor cerró la conexión TCP (apagado, caída de red, etc.). */
    void onServerDisconnected();

    // --- Eventos P2P distribuidos ---

    /** Un servidor se unió a la red. Recibe nodeId, host, clusterPort, message. */
    void onServerJoined(Map<String, Object> serverInfo);

    /** Un servidor se desconectó o fue declarado caído/sospechoso. */
    void onServerLeft(Map<String, Object> serverInfo);

    /** Lista actualizada de todos los servidores conocidos (ALIVE/SUSPECTED/DOWN). */
    void onPeerInfoUpdated(List<Map<String, Object>> servers);

    /** Logs consolidados de todos los servidores (local + remotos). */
    void onPeerLogsReceived(List<Map<String, Object>> peerLogs);

    // --- Eventos de Productos y Reseñas ---

    /** Lista actualizada de productos (documentos mapeados como entidad Producto). */
    void onProductosActualizados(List<Map<String, Object>> productos);

    /** Lista actualizada de reseñas asociadas a un producto. */
    void onResenasActualizadas(List<Map<String, Object>> resenas);

    /** Resultado automático del análisis de sentimiento de una reseña específica. */
    void onResenaAnalizada(String resenaId, String sentimiento, double confianza);
}

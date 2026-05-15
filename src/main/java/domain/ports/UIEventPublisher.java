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

    // --- Eventos P2P distribuidos ---

    /** Un servidor se unió a la red. Recibe nodeId, host, clusterPort, message. */
    void onServerJoined(Map<String, Object> serverInfo);

    /** Un servidor se desconectó o fue declarado caído/sospechoso. */
    void onServerLeft(Map<String, Object> serverInfo);

    /** Lista actualizada de todos los servidores conocidos (ALIVE/SUSPECTED/DOWN). */
    void onPeerInfoUpdated(List<Map<String, Object>> servers);

    /** Logs consolidados de todos los servidores (local + remotos). */
    void onPeerLogsReceived(List<Map<String, Object>> peerLogs);
}

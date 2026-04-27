package domain.ports;

import java.util.List;
import java.util.Map;

public interface UIEventPublisher {
    void onClientsUpdated(List<Map<String, Object>> clients);
    void onLogsUpdated(List<Map<String, Object>> logs);
    void onMessagesUpdated(List<Map<String, Object>> messages);
    void onDocumentsUpdated(List<Map<String, Object>> documents);
    void onUploadStatus(boolean success, String message);
    void onDownloadFinished(boolean success, String filename);
    void onConnectAck(String status, String message);
    void onUploadInitAck(String token);
    void onDownloadInitAck(String token, long size);
}

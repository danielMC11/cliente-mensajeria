package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

public class UploadStatusHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;
    private final String statusType;

    public UploadStatusHandler(UIEventPublisher uiPublisher, String statusType) {
        this.uiPublisher = uiPublisher;
        this.statusType = statusType;
    }

    @Override
    public String getActionType() {
        return statusType; // "UPLOAD_SUCCESS" o "UPLOAD_FAILED"
    }

    @Override
    public void handle(Map<String, Object> payload) {
        boolean success = "UPLOAD_SUCCESS".equals(statusType);
        String msg = success ? "Archivo subido con éxito" : "Error al subir el archivo";
        uiPublisher.onUploadStatus(success, msg);
    }
}

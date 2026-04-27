package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

public class DownloadInitAckHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public DownloadInitAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "DOWNLOAD_INIT_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload != null && "SUCCESS".equals(payload.get("status"))) {
            String token = (String) payload.get("message");
            long size = 0;
            try {
                Object sizeObj = payload.get("size_bytes");
                if (sizeObj != null) {
                    size = Double.valueOf(sizeObj.toString()).longValue();
                }
            } catch (Exception e) {
                System.err.println("Error parsing size_bytes: " + e.getMessage());
            }
            uiPublisher.onDownloadInitAck(token, size);
        }
    }
}

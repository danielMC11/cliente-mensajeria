package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

public class UploadInitAckHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public UploadInitAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "UPLOAD_INIT_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload != null && "SUCCESS".equals(payload.get("status"))) {
            String token = (String) payload.get("message");
            uiPublisher.onUploadInitAck(token);
        }
    }
}

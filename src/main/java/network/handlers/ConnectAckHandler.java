package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

public class ConnectAckHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ConnectAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "CONNECT_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload != null) {
            String status = (String) payload.get("status");
            String message = (String) payload.get("message");
            uiPublisher.onConnectAck(status, message);
        }
    }
}

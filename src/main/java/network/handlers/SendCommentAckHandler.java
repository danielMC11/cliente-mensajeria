package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.Map;

public class SendCommentAckHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;

    public SendCommentAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "REGISTER_COMMENT_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload == null) return;
        String status = String.valueOf(payload.getOrDefault("status", "?"));
        String message = String.valueOf(payload.getOrDefault("message", ""));
        System.out.println("[REGISTER_COMMENT_ACK] " + status + ": " + message);

        uiPublisher.SendCommentAckHandler(status, message);
    }
}

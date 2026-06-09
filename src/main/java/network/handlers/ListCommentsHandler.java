package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.List;
import java.util.Map;

public class ListCommentsHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListCommentsHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_COMMENTS_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("comments")) {
            List<Map<String, Object>> comments = (List<Map<String, Object>>) payload.get("comments");
            uiPublisher.onCommentsUpdated(comments);
        }
    }
}
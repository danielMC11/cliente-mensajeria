package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.List;
import java.util.Map;

public class ListMessagesHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListMessagesHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_MESSAGES_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("mensajes")) {
            List<Map<String, Object>> mensajes = (List<Map<String, Object>>) payload.get("mensajes");
            uiPublisher.onMessagesUpdated(mensajes);
        }
    }
}

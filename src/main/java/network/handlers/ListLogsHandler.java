package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.List;
import java.util.Map;

public class ListLogsHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListLogsHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_LOGS_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("logs")) {
            List<Map<String, Object>> logs = (List<Map<String, Object>>) payload.get("logs");
            uiPublisher.onLogsUpdated(logs);
        }
    }
}

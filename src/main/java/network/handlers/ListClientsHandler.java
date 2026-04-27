package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.List;
import java.util.Map;

public class ListClientsHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListClientsHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_CLIENTS_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("clientes")) {
            List<Map<String, Object>> clientes = (List<Map<String, Object>>) payload.get("clientes");
            uiPublisher.onClientsUpdated(clientes);
        }
    }
}

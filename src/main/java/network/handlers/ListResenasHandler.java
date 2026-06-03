package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListResenasHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListResenasHandler(UIEventPublisher uiPublisher) {
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
            // Filtramos solo los mensajes que tienen productId (son reseñas)
            List<Map<String, Object>> resenas = mensajes.stream()
                .filter(m -> m.containsKey("productId") && m.get("productId") != null)
                .collect(Collectors.toList());
            uiPublisher.onResenasActualizadas(resenas);
        }
    }
}

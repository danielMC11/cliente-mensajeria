package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.List;
import java.util.Map;

public class ListProductosHandler implements MessageHandler {
    private final UIEventPublisher uiPublisher;

    public ListProductosHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_DOCUMENTS_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("documentos")) {
            List<Map<String, Object>> documentos = (List<Map<String, Object>>) payload.get("documentos");
            uiPublisher.onProductosActualizados(documentos);
        }
    }
}

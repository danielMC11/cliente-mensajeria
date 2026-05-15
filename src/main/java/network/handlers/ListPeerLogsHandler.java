package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.List;
import java.util.Map;

/**
 * Procesa la respuesta LIST_PEER_LOGS_ACK del servidor.
 * Contiene los logs de todos los servidores de la red consolidados.
 *
 * Requerimiento: "Adicionar servicios para mostrar los logs de otros servidores."
 */
public class ListPeerLogsHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;

    public ListPeerLogsHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_PEER_LOGS_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("peerLogs")) {
            List<Map<String, Object>> peerLogs =
                    (List<Map<String, Object>>) payload.get("peerLogs");
            uiPublisher.onPeerLogsReceived(peerLogs);
        }
    }
}

package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.List;
import java.util.Map;

/**
 * Procesa la respuesta LIST_PEER_INFO_ACK del servidor.
 * Contiene la lista de todos los servidores conocidos en la red (ALIVE/SUSPECTED/DOWN).
 *
 * Requerimiento: "Detección de servidores amigos: listar servidores conectados y desconectados."
 */
public class ListPeerInfoHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;

    public ListPeerInfoHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "LIST_PEER_INFO_ACK";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("servidores")) {
            List<Map<String, Object>> servers =
                    (List<Map<String, Object>>) payload.get("servidores");
            uiPublisher.onPeerInfoUpdated(servers);
        }
    }
}

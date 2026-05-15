package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.Map;

/**
 * Procesa el push del servidor cuando un nuevo servidor peer se une a la red.
 * Acción: SERVER_JOINED
 *
 * Requerimiento: "Los servidores deberán informar el momento en que se une algún servidor."
 */
public class ServerJoinedHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;

    public ServerJoinedHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "SERVER_JOINED";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload != null) {
            uiPublisher.onServerJoined(payload);
        }
    }
}

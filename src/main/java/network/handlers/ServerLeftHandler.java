package network.handlers;

import domain.ports.UIEventPublisher;

import java.util.Map;

/**
 * Procesa los push del servidor cuando un servidor peer se desconecta o se vuelve sospechoso.
 * Acciones: SERVER_LEFT, SERVER_SUSPECTED
 *
 * Se instancia dos veces (una por acción) en VentanaConexion con el tipo correspondiente.
 *
 * Requerimiento: "Los servidores deberán informar el momento en que se desconecta algún servidor."
 */
public class ServerLeftHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;
    private final String actionType;  // "SERVER_LEFT" o "SERVER_SUSPECTED"

    public ServerLeftHandler(UIEventPublisher uiPublisher, String actionType) {
        this.uiPublisher = uiPublisher;
        this.actionType = actionType;
    }

    @Override
    public String getActionType() {
        return actionType;
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload != null) {
            // Enriquecer el payload con el tipo de evento para que la UI sepa si es LEFT o SUSPECTED
            payload.put("eventType", actionType);
            uiPublisher.onServerLeft(payload);
        }
    }
}

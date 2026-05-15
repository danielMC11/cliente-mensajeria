package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

/**
 * Handler para mensajes de texto recibidos en tiempo real.
 *
 * Se registra para DOS acciones:
 *   - "NEW_MESSAGE_ACK" → mensajes privados entregados directamente al socket
 *     (el servidor usa buildSuccessResponse que añade el sufijo _ACK).
 *   - "NEW_MESSAGE"     → broadcasts replicados desde otros nodos
 *     (el servidor los construye manualmente sin sufijo _ACK).
 *
 * Se instancia dos veces en VentanaConexion, una por cada acción.
 */
public class NewMessageHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;
    private final String actionType;

    /**
     * @param uiPublisher publicador hacia la UI
     * @param actionType  "NEW_MESSAGE_ACK" o "NEW_MESSAGE"
     */
    public NewMessageHandler(UIEventPublisher uiPublisher, String actionType) {
        this.uiPublisher = uiPublisher;
        this.actionType  = actionType;
    }

    @Override
    public String getActionType() {
        return actionType;
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload == null) return;

        // El campo del texto se llama "message" en ambos formatos
        Object raw = payload.get("message");
        if (raw == null) return;

        String message = String.valueOf(raw).trim();
        if (!message.isBlank()) {
            uiPublisher.onNewMessage(message);
        }
    }
}

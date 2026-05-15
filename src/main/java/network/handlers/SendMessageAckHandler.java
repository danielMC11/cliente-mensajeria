package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

/**
 * Handler para SEND_MESSAGE_ACK.
 * El servidor confirma que el mensaje fue entregado (o no).
 * También refresca la tabla de mensajes para que el REMITENTE
 * vea su mensaje enviado en el historial (privado o broadcast).
 */
public class SendMessageAckHandler implements MessageHandler {

    private final UIEventPublisher uiPublisher;

    public SendMessageAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "SEND_MESSAGE_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (payload == null) return;
        String status  = String.valueOf(payload.getOrDefault("status",  "?"));
        String message = String.valueOf(payload.getOrDefault("message", ""));
        System.out.println("[SEND_MESSAGE_ACK] " + status + ": " + message);

        // Refrescar la tabla para que el remitente vea su mensaje enviado
        // (especialmente importante para mensajes privados, ya que el remitente
        // no recibe NEW_MESSAGE_ACK, solo el receptor lo hace)
        uiPublisher.onNewMessage("");
    }
}

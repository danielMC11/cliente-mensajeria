package network.handlers;

import domain.ports.UIEventPublisher;
import java.util.Map;

public class AnalyzeMessageAckHandler implements MessageHandler {
    private UIEventPublisher uiPublisher;

    public AnalyzeMessageAckHandler(UIEventPublisher uiPublisher) {
        this.uiPublisher = uiPublisher;
    }

    @Override
    public String getActionType() {
        return "ANALYZE_MESSAGE_ACK";
    }

    @Override
    public void handle(Map<String, Object> payload) {
        if (uiPublisher != null) {
            String status = (String) payload.get("status");
            String sentimiento = (String) payload.get("sentimiento");
            Object confObj = payload.get("confianza_porcentaje");
            double confianza = 0.0;
            if (confObj instanceof Number) {
                confianza = ((Number) confObj).doubleValue();
            } else if (confObj instanceof String) {
                try {
                    confianza = Double.parseDouble((String) confObj);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            uiPublisher.onMessageAnalyzed(status, sentimiento, confianza);
        }
    }
}

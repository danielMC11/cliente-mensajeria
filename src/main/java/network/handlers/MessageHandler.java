package network.handlers;

import java.util.Map;

public interface MessageHandler {
    void handle(Map<String, Object> payload);
    String getActionType();
}

package network;

import network.handlers.MessageHandler;

import java.util.HashMap;
import java.util.Map;

public class ClientRouter {
    private final Map<String, MessageHandler> handlers = new HashMap<>();

    public void registerHandler(MessageHandler handler) {
        handlers.put(handler.getActionType(), handler);
    }

    @SuppressWarnings("unchecked")
    public void route(String json) {
        if (json == null || json.trim().isEmpty())
            return;
        System.out.println("Router recibió: " + json);
        try {
            Map<String, Object> map = JSONSerializer.deserialize(json, Map.class);
            String action = (String) map.get("action");
            
            if (action == null) {
                if (map.containsKey("status")) {
                    String status = (String) map.get("status");
                    if (handlers.containsKey(status)) {
                        handlers.get(status).handle(map);
                    }
                }
                return;
            }

            if (handlers.containsKey(action)) {
                Map<String, Object> payload = (Map<String, Object>) map.get("payload");
                handlers.get(action).handle(payload);
            } else {
                System.out.println("No handler registered for action: " + action);
            }

        } catch (Exception e) {
            System.err.println("Error ruteando mensaje: " + json);
            e.printStackTrace();
        }
    }
}


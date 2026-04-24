package config.models;

import java.util.Map;

public class MessageRequest {
    private String action;
    private Long userId;
    private Map<String, Object> payload;

    public MessageRequest(String action, Map<String, Object> payload) {
        this.action = action;
        this.payload = payload;
    }

    public MessageRequest(String action, Long userId, Map<String, Object> payload) {
        this.action = action;
        this.userId = userId;
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public Long getUserId() {
        return userId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}

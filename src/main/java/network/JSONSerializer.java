package network;

import com.google.gson.Gson;

public class JSONSerializer {
    private static final Gson gson = new Gson();

    public static String serialize(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}

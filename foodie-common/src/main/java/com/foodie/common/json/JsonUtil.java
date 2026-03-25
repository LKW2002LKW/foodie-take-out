package com.foodie.common.json;

/**
 * Lightweight JSON utility skeleton. Implementation can delegate to JacksonObjectMapper.
 */
public class JsonUtil {

    private static final JacksonObjectMapper MAPPER = new JacksonObjectMapper();

    private JsonUtil() {}

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization error", e);
        }
    }

}


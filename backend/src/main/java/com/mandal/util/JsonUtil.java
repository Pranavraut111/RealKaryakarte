package com.mandal.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Shared Jackson ObjectMapper instance and convenience methods for
 * reading JSON request bodies and writing JSON responses in Servlets.
 */
public class JsonUtil {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // Handle Java 8+ date/time types (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private JsonUtil() {}

    /**
     * Get the shared ObjectMapper (for advanced use — prefer the helper methods below).
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Read the request body as a Java object.
     */
    public static <T> T readBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return mapper.readValue(sb.toString(), clazz);
    }

    /**
     * Write a Java object as JSON to the response.
     * Sets content type and status code.
     */
    public static void writeResponse(HttpServletResponse response, int statusCode, Object obj) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        mapper.writeValue(response.getWriter(), obj);
    }

    /**
     * Shorthand: write 200 OK with a JSON body.
     */
    public static void writeOk(HttpServletResponse response, Object obj) throws IOException {
        writeResponse(response, 200, obj);
    }

    /**
     * Shorthand: write an error response with the given status code.
     */
    public static void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        writeResponse(response, statusCode, new ErrorBody(message));
    }

    /**
     * Simple error body for consistent error JSON.
     */
    public static class ErrorBody {
        public boolean success = false;
        public String message;

        public ErrorBody(String message) {
            this.message = message;
        }
    }
}

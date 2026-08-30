package com.mandal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PasswordStore {
    private static final String FILE_PATH = "/Users/pranavraut/RealKaryakarte/backend/passwords.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ConcurrentHashMap<Long, String> passwords = new ConcurrentHashMap<>();

    static {
        load();
    }

    private static void load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                Map<Long, String> data = mapper.readValue(file, new TypeReference<Map<Long, String>>() {});
                passwords.putAll(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void save() {
        try {
            mapper.writeValue(new File(FILE_PATH), passwords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setPassword(Long userId, String hash) {
        passwords.put(userId, hash);
        save();
    }

    public static String getPassword(Long userId) {
        return passwords.get(userId);
    }
}

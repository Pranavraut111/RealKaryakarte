package com.mandal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * File-based password hash store.
 * Stores hashed passwords in a JSON file on disk (never plaintext).
 * Path is configurable via ConfigUtil ("storage.base.dir") or defaults to ~/mandal_data.
 */
public class PasswordStore {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ConcurrentHashMap<Long, String> passwords = new ConcurrentHashMap<>();
    private static final String FILE_PATH;

    static {
        String baseDir = ConfigUtil.get("storage.base.dir",
                System.getProperty("user.home") + "/mandal_data");
        FILE_PATH = baseDir + "/passwords.json";
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
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs(); // ensure directory exists
            mapper.writeValue(file, passwords);
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

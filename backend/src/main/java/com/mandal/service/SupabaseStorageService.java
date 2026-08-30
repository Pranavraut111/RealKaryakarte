package com.mandal.service;

import com.mandal.util.ConfigUtil;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseStorageService {

    private static final String SUPABASE_URL = ConfigUtil.get("supabase.url");
    private static final String SUPABASE_KEY = ConfigUtil.get("supabase.key");
    private static final String BUCKET_NAME = "mandal-assets";

    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Uploads a file (byte array) to Supabase Storage.
     *
     * @param fileName The name of the file to save as (e.g. "receipt.pdf").
     * @param content The file content as a byte array.
     * @param contentType The MIME type (e.g. "application/pdf").
     * @return The public URL of the uploaded file, or null if upload fails.
     */
    public static String uploadFile(String fileName, byte[] content, String contentType) {
        if (SUPABASE_URL == null || SUPABASE_KEY == null) {
            System.err.println("[SupabaseStorageService] SUPABASE_URL or SUPABASE_KEY not configured.");
            return null;
        }

        try {
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("apikey", SUPABASE_KEY)
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
            } else {
                System.err.println("[SupabaseStorageService] Failed to upload: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Uploads a file from an InputStream to Supabase Storage.
     */
    public static String uploadFile(String fileName, InputStream content, String contentType) {
        if (SUPABASE_URL == null || SUPABASE_KEY == null) {
            System.err.println("[SupabaseStorageService] SUPABASE_URL or SUPABASE_KEY not configured.");
            return null;
        }

        try {
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("apikey", SUPABASE_KEY)
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> content))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
            } else {
                System.err.println("[SupabaseStorageService] Failed to upload: " + response.statusCode() + " " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

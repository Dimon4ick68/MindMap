package com.mindapp.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindapp.client.models.MindMap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8081/api/maps";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper(); // Для JSON

    // Отримати всі мапи користувача (GET)
    public List<MindMap> getMaps(String userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?userId=" + userId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<List<MindMap>>(){});
        } else {
            throw new RuntimeException("Помилка завантаження: " + response.statusCode());
        }
    }
    public boolean register(String username, String password) {
        try {
            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) { return false; }
    }

public boolean login(String username, String password) {
        try {
            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            // --- ОСЬ ЦЬОГО РЯДКА НЕ ВИСТАЧАЛО ---
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // ------------------------------------

            return response.statusCode() == 200;
        } catch (Exception e) { return false; }
    }

    public void deleteMap(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Помилка видалення: " + response.statusCode());
        }
    }

    // Зберегти мапу (POST)
    public MindMap saveMap(MindMap map) throws Exception {
        String json = mapper.writeValueAsString(map);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), MindMap.class);
        } else {
            throw new RuntimeException("Помилка збереження: " + response.statusCode());
        }
    }
}
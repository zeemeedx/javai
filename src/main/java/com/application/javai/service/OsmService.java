package com.application.javai.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.application.javai.dto.PlaceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OsmService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OsmService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://overpass-api.de/api")
                .defaultHeader(HttpHeaders.USER_AGENT, "javai-app/1.0 (seu-email@example.com)")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<PlaceDTO> buscarLugares(
            double lat,
            double lon,
            int radiusMeters,
            String amenityRegex
    ) {
        try {
            String overpassQuery = String.format(
                    Locale.US, // 👈 força ponto como separador decimal
                    """
                    [out:json][timeout:25];
                    (
                      node["amenity"~"%s"](around:%d,%.6f,%.6f);
                      way["amenity"~"%s"](around:%d,%.6f,%.6f);
                    );
                    out center;
                    """,
                    amenityRegex, radiusMeters, lat, lon,
                    amenityRegex, radiusMeters, lat, lon
            );

            System.out.println("=== Overpass QL ===");
            System.out.println(overpassQuery);

            String body = "data=" + URLEncoder.encode(overpassQuery, StandardCharsets.UTF_8);

            String responseJson = webClient.post()
                    .uri("/interpreter")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("=== Overpass Response (inicio) ===");
            if (responseJson != null && responseJson.length() > 500) {
                System.out.println(responseJson.substring(0, 500) + "...");
            } else {
                System.out.println(responseJson);
            }

            return parseOverpassResponse(responseJson);

        } catch (Exception e) {
            System.err.println("Erro ao consultar Overpass: " + e.getMessage());
            return List.of();
        }
    }

    private List<PlaceDTO> parseOverpassResponse(String json) throws Exception {
        List<PlaceDTO> result = new ArrayList<>();

        if (json == null || json.isBlank()) {
            System.err.println("Resposta vazia do Overpass");
            return result;
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode elements = root.get("elements");
        if (elements == null || !elements.isArray()) {
            System.err.println("Campo 'elements' ausente ou não é array na resposta do Overpass");
            return result;
        }

        long idCounter = 1L;

        for (JsonNode el : elements) {
            JsonNode tags = el.get("tags");
            if (tags == null) continue;

            String name = tags.has("name") ? tags.get("name").asText() : "(sem nome)";
            String type = tags.has("amenity") ? tags.get("amenity").asText() : "unknown";

            String elementType = el.get("type").asText();
            double lat;
            double lon;

            if ("node".equals(elementType)) {
                lat = el.get("lat").asDouble();
                lon = el.get("lon").asDouble();
            } else {
                JsonNode center = el.get("center");
                if (center == null) continue;
                lat = center.get("lat").asDouble();
                lon = center.get("lon").asDouble();
            }

            PlaceDTO dto = new PlaceDTO(
                    idCounter++,
                    name,
                    type,
                    lat,
                    lon
            );
            result.add(dto);
        }

        System.out.println("Total de elementos convertidos em PlaceDTO: " + result.size());
        return result;
    }
}

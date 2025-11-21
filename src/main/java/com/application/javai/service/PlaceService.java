package com.application.javai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.application.javai.dto.PlaceDTO;

@Service
public class PlaceService {

    private final OsmService osmService;

    public PlaceService(OsmService osmService) {
        this.osmService = osmService;
    }

    public List<PlaceDTO> buscarPlaces(double lat, double lon, int radiusMeters) {
        // Regex genérico para buscar qualquer coisa que tenha a tag "amenity"
        String amenityRegex = "."; 
        List<PlaceDTO> fromOsm = osmService.buscarLugares(lat, lon, radiusMeters, amenityRegex);

        if (fromOsm.isEmpty()) {
            System.out.println("Nenhum lugar vindo do OSM. (lat=" + lat + ", lon=" + lon + ")");
        }

        return fromOsm;
    }

    // Método de conveniência pro Rio (se quiser manter)
    public List<PlaceDTO> listarPlacesDoRioPadrao() {
        double rioLat = -22.9068;
        double rioLon = -43.1729;
        int radiusMeters = 2000;
        return buscarPlaces(rioLat, rioLon, radiusMeters);
    }
}

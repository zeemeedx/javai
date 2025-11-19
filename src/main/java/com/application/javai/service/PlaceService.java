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

    /**
     * Busca bares, restaurantes e cafés no Rio de Janeiro
     */
    // public List<PlaceDTO> listarPlacesDoRio() {
    //     double rioLat = -22.9068;
    //     double rioLon = -43.1729;
    //     int radiusMeters = 2000; // 2km de raio
    //     String amenityRegex = "bar|restaurant|cafe";

    //     //return osmService.buscarLugares(rioLat, rioLon, radiusMeters, amenityRegex);
    //     // MOCK: só pra testar se o mapa + controller + DTO estão ok
    //     return List.of(
    //         new PlaceDTO(1L, "Bar da Lapa", "bar", -22.9122, -43.1799),
    //         new PlaceDTO(2L, "Praia de Copacabana", "praia", -22.9711, -43.1822),
    //         new PlaceDTO(3L, "Parque do Flamengo", "parque", -22.9296, -43.1708),
    //         new PlaceDTO(4L, "Ciclovia da Lagoa", "ciclovia", -22.9670, -43.2100)
    //     );
    // }
    public List<PlaceDTO> listarPlacesDoRio() {
        double rioLat = -22.9068;
        double rioLon = -43.1729;
        int radiusMeters = 4000; // aumentei um pouco
        String amenityRegex = "bar|restaurant|cafe|pub|fast_food";

        return osmService.buscarLugares(rioLat, rioLon, radiusMeters, amenityRegex);
    }
}


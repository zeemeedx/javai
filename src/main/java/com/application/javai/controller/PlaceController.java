package com.application.javai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.javai.dto.PlaceDTO;
import com.application.javai.service.PlaceService;

@RestController
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/api/places")
    public List<PlaceDTO> listarPlaces(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "4000") Integer radius
    ) {
        // Se não vier lat/lon, usa Rio de Janeiro como padrão
        if (lat == null || lon == null) {
            System.out.println("Lat/lon não informados. Usando Rio de Janeiro como padrão.");
            return placeService.listarPlacesDoRioPadrao();
        }

        return placeService.buscarPlaces(lat, lon, radius);
    }
}

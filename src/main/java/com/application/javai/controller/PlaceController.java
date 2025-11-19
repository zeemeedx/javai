package com.application.javai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.javai.dto.PlaceDTO;
import com.application.javai.service.PlaceService;

@RestController
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/api/places")
    public List<PlaceDTO> listarPlaces() {
        // aqui você pode futuramente receber parâmetros (lat, lon, radius etc.)
        return placeService.listarPlacesDoRio();
    }
}

package com.application.javai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.application.javai.dto.PlaceDTO;

@Service
public class PlaceService {

    // por enquanto: dados mockados
    public List<PlaceDTO> listarPlacesDoRio() {
        return List.of(
            new PlaceDTO(1L, "Bar da Lapa", "bar", -22.9122, -43.1799),
            new PlaceDTO(2L, "Praia de Copacabana", "praia", -22.9711, -43.1822),
            new PlaceDTO(3L, "Parque do Flamengo", "parque", -22.9296, -43.1708),
            new PlaceDTO(4L, "Ciclovia da Lagoa", "ciclovia", -22.9670, -43.2100)
        );
    }
}

package com.application.javai.dto;

public class PlaceDTO {

    private Long id;
    private String name;
    private String type;    // bar, restaurant, praça, ciclovia, etc.
    private double lat;
    private double lon;

    public PlaceDTO() {
    }

    public PlaceDTO(Long id, String name, String type, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

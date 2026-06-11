package com.truebalance.creditiq.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeoService {

    private static final Logger log = LoggerFactory.getLogger(GeoService.class);
    private static final String GEO_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client";

    private final RestClient restClient;

    public GeoService() {
        this.restClient = RestClient.builder()
                .baseUrl(GEO_URL)
                .build();
    }

    public String resolveCity(Double lat, Double lng) {
        if (lat == null || lng == null) return null;
        try {
            var response = restClient.get()
                    .uri("?latitude={lat}&longitude={lng}&localityLanguage=en", lat, lng)
                    .retrieve()
                    .body(GeoResponse.class);
            if (response != null && response.city != null && !response.city.isBlank()) {
                log.info("Geo resolved: ({}, {}) -> {}", lat, lng, response.city);
                return response.city;
            }
        } catch (Exception e) {
            log.warn("Geo resolution failed for ({}, {}): {}, defaulting to Gurugram", lat, lng, e.getMessage());
        }
        return "Gurugram";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeoResponse(String city, String locality, String countryName, String principalSubdivision) {}
}

package com.example.shopService.support.geolocation;

import com.example.shopService.domain.ShopEntity;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.PendingResult;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GeolocationService {

    private static final double R = 6372.8; // In kilometers

    /**
     * From https://rosettacode.org/wiki/Haversine_formula#Java
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public CompletableFuture<Coordinates> geoLocate(ShopEntity shop) {
        CompletableFuture<Coordinates> completableFuture = new CompletableFuture<>();
        GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBkNfsZ867XNB90YE0aY-uAQmXZHCeAocA"); // TODO externalize API_KEY
        GeocodingApiRequest req = GeocodingApi.newRequest(context)
                .components(ComponentFilter.country("GB"), ComponentFilter.postalCode(shop.getShopAddress().getPostCode()));
        //.address(shop.getShopAddress().getNumber()); // TODO Add complete address for better geolocation
        req.setCallback(new PendingResult.Callback<GeocodingResult[]>() {
            @Override
            public void onResult(GeocodingResult[] result) {
                if (result != null && result.length > 0) {
                    Geometry geometry = result[0].geometry;
                    completableFuture.complete(new Coordinates(geometry.location.lat, geometry.location.lng));
                }
            }

            @Override
            public void onFailure(Throwable e) {
                log.error("Could not geocode", e);
            }
        });
        return completableFuture;
    }


}
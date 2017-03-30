package com.example.shopService.domain;

import com.example.shopService.support.geolocation.GeolocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ShopService {

    private final ShopRepository shopRepository;

    private final GeolocationService geolocationService;

    @Autowired
    public ShopService(ShopRepository shopRepository, GeolocationService geolocationService) {
        this.shopRepository = shopRepository;
        this.geolocationService = geolocationService;
    }

    public ShopEntity get(String id) {
        return shopRepository.findOne(id);
    }

    public List<ShopEntity> list() {
        return this.shopRepository.findAll();
    }

    public ShopEntity listByProximity(Double lat, Double lng) {
        log.info("sorting by proximity with " + lat + "/" + lng);
        List<ShopEntity> shops = list();
        if (shops == null || shops.size() == 0) {
            return null;
        } else {
            shops.sort((ShopEntity shop1, ShopEntity shop2) -> {
                if (shop1.getShopAddress().getLatitude() == null
                        || shop1.getShopAddress().getLongitude() == null) {
                    return -1;
                } else if (shop2.getShopAddress().getLatitude() == null
                        || shop2.getShopAddress().getLongitude() == null) {
                    return 1;
                } else {
                    double shop1Distance = GeolocationService.getDistance(
                            shop1.getShopAddress().getLatitude(),
                            shop1.getShopAddress().getLongitude(),
                            lat,
                            lng);

                    double shop2Distance = GeolocationService.getDistance(
                            shop2.getShopAddress().getLatitude(),
                            shop2.getShopAddress().getLongitude(),
                            lat,
                            lng);

                    return Double.compare(shop1Distance, shop2Distance);
                }
            });

            return shops.get(0);
        }
    }

    public Optional<ShopEntity> save(ShopEntity shop) {
        Optional<ShopEntity> existingShop = shopRepository.save(shop.getName(), shop);
        geolocateShop(shop);
        return existingShop;
    }

    /**
     * Considered the geolocation as an enrichment process.
     * Treating it as an asynchronous task
     * Implemented a basic versioning system to avoid possible race conditions
     */
    private void geolocateShop(ShopEntity shop) {
        geolocationService.geoLocate(shop).thenAccept(coordinates -> {
            log.debug("coordinates  " + coordinates);
            ShopEntity savedShop = shopRepository.findOne(shop.getName());
            if (Objects.equals(savedShop.getVersion(), shop.getVersion())) {
                savedShop.getShopAddress().setLatitude(coordinates.getLatitude());
                savedShop.getShopAddress().setLongitude(coordinates.getLongitude());
                shopRepository.save(savedShop.getName(), savedShop);
            } else {
                log.debug("trying to set coordinates of an older version of " + shop.getName());
            }
        });
    }
}

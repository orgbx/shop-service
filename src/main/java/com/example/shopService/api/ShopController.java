package com.example.shopService.api;

import com.example.shopService.api.resources.ShopResourceAssembler;
import com.example.shopService.api.resources.ShopResourceAssembler.ShopResource;
import com.example.shopService.domain.ShopEntity;
import com.example.shopService.domain.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/shops", produces = MediaTypes.HAL_JSON_VALUE)
@ExposesResourceFor(ShopResource.class)
public class ShopController {

    private final ShopService shopService;

    private final ShopResourceAssembler shopResourceAssembler;

    private final EntityLinks entityLinks;

    @Autowired
    public ShopController(ShopService shopService, ShopResourceAssembler shopResourceAssembler, EntityLinks entityLinks) {
        this.shopService = shopService;
        this.shopResourceAssembler = shopResourceAssembler;
        this.entityLinks = entityLinks;
    }

    @GetMapping
    public HttpEntity<? extends ResourceSupport> findAll(
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng) {
        Link link;
        if (lat == null || lng == null) {
            Iterable<ShopEntity> shops = shopService.list();
            link = this.entityLinks.linkToCollectionResource(ShopResource.class);
            Resources<ShopResource> resources = new Resources<>(shopResourceAssembler.toResources(shops));
            resources.add(link);
            return new ResponseEntity<>(resources, HttpStatus.OK);
        } else {
            ShopEntity closestShop = shopService.listByProximity(lat, lng);
            if (closestShop == null) {
                return ResponseEntity.noContent().build();
            }
            ShopResource resource = shopResourceAssembler.toResource(closestShop);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
    }

    @PostMapping
    public HttpEntity<ShopResource> save(@RequestBody ShopEntity shop) {
        validateShop(shop);
        Optional<ShopEntity> previousShopVersion = this.shopService.save(shop);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shopResourceAssembler.toResource(shop).getLink(Link.REL_SELF).getHref()));
        if (!previousShopVersion.isPresent()) {
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        } else {
            ShopResource resource = shopResourceAssembler.toResource(previousShopVersion.get());
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        }
    }

    private void validateShop(ShopEntity shop) {
        Assert.notNull(shop, "Shop object expected");
        Assert.notNull(shop.getName(), "Shop.name expected");
        Assert.notNull(shop.getShopAddress(), "Shop.shopAddress expected");
        Assert.notNull(shop.getShopAddress().getNumber(), "Shop.shopAddress.number expected");
        Assert.notNull(shop.getShopAddress().getPostCode(), "Shop.shopAddress.postCode expected");
    }

    @GetMapping(path = "/{name}")
    public HttpEntity<ShopResource> findOne(@PathVariable String name) {
        log.debug("received request");
        ShopEntity shop = shopService.get(name);
        if (shop == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            ShopResource resource = shopResourceAssembler.toResource(shop);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
    }
}

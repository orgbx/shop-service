package com.example.shopService.api.resources;

import com.example.shopService.api.ShopController;
import com.example.shopService.domain.ShopEntity;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Component
public class ShopResourceAssembler extends ResourceAssemblerSupport<ShopEntity, ShopResourceAssembler.ShopResource> {

    public ShopResourceAssembler() {
        super(ShopController.class, ShopResource.class);
    }

    @Override
    public ShopResource toResource(final ShopEntity ShopEntity) {
        final ShopResource resource = new ShopResource(ShopEntity);

        resource.add(linkTo(methodOn(ShopController.class, ShopEntity.getName()).findOne(ShopEntity.getName()))
                .withSelfRel());

        return resource;
    }

    public static class ShopResource extends Resource<ShopEntity> {

        ShopResource(final ShopEntity shop) {
            super(shop);
        }


    }

}
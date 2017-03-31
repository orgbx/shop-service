package com.example.shopService;

import com.example.shopService.domain.ShopEntity;
import com.example.shopService.domain.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ShopIntegrationTest {

    @Autowired
    private
    ShopRepository shopRepository;
    @Autowired
    private MockMvc mockMvc;
    private List<ShopEntity> setupShops;
    private ShopEntity newShop;


    @Before
    public void setUp() throws Exception {
        setupShops = Arrays.asList(
                new ShopEntity("shop1", new ShopEntity.ShopAddress("number_1", "WC2E 8HD")),
                new ShopEntity("shop2", new ShopEntity.ShopAddress("number_2", "DN36 5YG")),
                new ShopEntity("shop3", new ShopEntity.ShopAddress("number_3", "KT20 6SD"))
        );

        newShop = new ShopEntity("shop4", new ShopEntity.ShopAddress("number_4", "WC1E 8HD"));

        for (ShopEntity shop : setupShops) {
            shopRepository.save(shop.getName(), shop);
        }
        log.debug("shops created");
    }

    @After
    public void tearDown() throws Exception {
        shopRepository.deleteAll();
    }

    @Test
    public void findAll() throws Exception {
        this.mockMvc
                .perform(get("/shops/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.shopEntityList.length()").value(setupShops.size()));
    }

    @Test
    public void saveInvalidShop() throws Exception {
        this.mockMvc
                .perform(post("/shops/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
    }

    @Test
    public void saveNonExistantShop() throws Exception {
        this.mockMvc
                .perform(post("/shops/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(getShopAsJSON(newShop)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/shops/shop4"))
                .andExpect(content().string(""));
    }

    @Test
    public void saveExistingShop() throws Exception {
        ShopEntity shopUpdate = new ShopEntity(setupShops.get(0).getName(), new ShopEntity.ShopAddress("number_X", "SY23 4BH"));
        String previousPostCode = setupShops.get(0).getShopAddress().getPostCode();

        this.mockMvc
                .perform(post("/shops/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(getShopAsJSON(shopUpdate)))
                .andExpect(status().isOk())
                .andExpect(header().string("location", "http://localhost/shops/shop1"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.shopAddress.postCode").value(previousPostCode));
    }

    @Test
    public void getNonExistantShop() throws Exception {
        this.mockMvc
                .perform(get("/shops/devnull"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getExistingShop() throws Exception {
        this.mockMvc
                .perform(get("/shops/" + setupShops.get(0).getName()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"version\":0,\"name\":\"shop1\",\"shopAddress\":{\"number\":\"number_1\",\"postCode\":\"WC2E 8HD\",\"longitude\":null,\"latitude\":null},\"_links\":{\"self\":{\"href\":\"http://localhost/shops/shop1\"}}}"));
    }

    private String getShopAsJSON(ShopEntity shop) {
        return "{\n" +
                "\t\"name\": \"" + shop.getName() + "\",\n" +
                "\t\"shopAddress\" : {\n" +
                "\t\t\"number\": \"" + shop.getShopAddress().getNumber() + "\",\n" +
                "\t\t\"postCode\": \"" + shop.getShopAddress().getPostCode() + "\"\n" +
                "\t}\n" +
                "}";
    }

}
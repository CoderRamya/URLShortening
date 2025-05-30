package com.link.shortener.controller;

import com.link.shortener.entity.URLEntity;
import com.link.shortener.services.URLShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(URLShortenerController.class)
public class URLShortenerControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    URLShortenerService linkShortenerService;

    @BeforeEach
    public void beforeTest() {
        when(linkShortenerService.findIfEntityExistsByShortCode("abc123")).thenReturn(true);

    }

    @Test
    public void test_create() throws Exception {
        String longURL="http://example.com";
        when(linkShortenerService.save(anyString())).thenReturn(
                new URLEntity(longURL, "abc123", 0,
                        Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now())));
        mvc.perform(MockMvcRequestBuilders.post("/shorten")
                .content(longURL)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longURL").value("http://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"));

    }


    @Test
    public void test_get() throws Exception {
        String shortCode = "abc123";
        when(linkShortenerService.getLongURlAndUpdateAccessCount(shortCode)).thenReturn(Optional.of(new URLEntity("http://example.com",
                "abc123", 0,Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()))));

        mvc.perform(MockMvcRequestBuilders.get("/shorten/" + shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longURL").value("http://example.com"));
    }

    @Test
    public void test_getStats() throws Exception {
        String shortCode = "abc123";
        URLEntity entity = new URLEntity("http://example.com", shortCode, 5,
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        when(linkShortenerService.getLongURl(shortCode)).thenReturn(Optional.of(entity));

        mvc.perform(MockMvcRequestBuilders.get("/shorten/" + shortCode + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longURL").value("http://example.com"))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.accessCount").value(5));
    }


    @Test
    public void test_update() throws Exception {
        String shortCode = "abc123";
        String newLongURL = "http://newexample.co.in";
        URLEntity updatedEntity = new URLEntity(newLongURL, shortCode, 0,
                Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        when(linkShortenerService.update(shortCode, newLongURL)).thenReturn(updatedEntity);

        mvc.perform(MockMvcRequestBuilders.put("/shorten/" + shortCode)
                .content(newLongURL)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longURL").value(newLongURL))
                .andExpect(jsonPath("$.shortCode").value(shortCode));
    }

    @Test
    public void test_delete() throws Exception {
        String shortCode = "abc123";
        mvc.perform(MockMvcRequestBuilders.delete("/shorten/" + shortCode))
                .andExpect(status().isAccepted());
    }






}

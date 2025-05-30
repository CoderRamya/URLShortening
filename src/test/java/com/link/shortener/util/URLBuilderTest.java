package com.link.shortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class URLBuilderTest {


    // This class is intended for testing URL building functionality.
    // It should contain methods to test various aspects of URL construction,
    // such as encoding, decoding, and validation of URLs.

    @Test
    public void testBuildURL() {
        String longURL = "http://example.com";
        String code = URLBuilder.encodeBase64(longURL);
        assertEquals(6, code.length());
    }

    @Test
    public void test_uniqueness(){

    String longURL1 = "http://example.com";
    String longURL2= "http://example.org";
    String code1 = URLBuilder.encodeBase64(longURL1);
    String code2 = URLBuilder.encodeBase64(longURL2);
    assertNotEquals(code1, code2, "Encoded URLs should be unique");


    }

    @Test
    public void test_nospecialcharacters() {
        String longURL = "http://example.com";
        String code = URLBuilder.encodeBase64(longURL);
        // Assuming the encoding does not produce special characters
        assertEquals(false, code.matches(".*[!@#$%^&*(),.?\":{}|<>].*"), "Encoded URL should not contain special characters");
    }
}

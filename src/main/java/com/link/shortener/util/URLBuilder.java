package com.link.shortener.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class URLBuilder {

    private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String encodeBase64(String longURL) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(longURL.getBytes());
            // Use the first 4 bytes of the hash to get a positive integer
            int id = ((hash[0] & 0xFF) << 24) | ((hash[1] & 0xFF) << 16) | ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF);
            id = Math.abs(id);
            StringBuilder sb = new StringBuilder();
            while (id > 0) {
                sb.append(BASE64.charAt(id % 62));
                id /= 62;
            }
            java.util.Random random = new java.util.Random();
            while (sb.length() < 6) {
                sb.append(BASE64.charAt(random.nextInt(62)));
            }
            return sb.reverse().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
package com.demo.dto;

public class ShortenResponse {
    private String shortCode; // abcde
    private String shortUrl; // http://localhost:8080/abcde
    private String originalUrl; 

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}

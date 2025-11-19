package com.demo.controller;

import com.demo.dto.ShortenResponse;
import com.demo.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AccessUrlController {

    private final UrlService urlService;

    public AccessUrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // Public redirect for short URL
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        ShortenResponse response = urlService.findByCode(code);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", response.getOriginalUrl())
                .build();
    }
}

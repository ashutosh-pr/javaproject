package com.demo.controller;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.demo.dto.ShortenRequest;
import com.demo.dto.ShortenResponse;
import com.demo.service.UrlService;

@RestController
@RequestMapping("/api")
public class ShortenController {

    private final UrlService urlService;

    public ShortenController(UrlService urlService) {
        this.urlService = urlService;
    }

    // @PostMapping("/shorten")
    // public ResponseEntity<ShortenResponse> shorten(@RequestBody @Valid
    // ShortenRequest request) {
    // return ResponseEntity.ok(urlService.shorten(request));
    // }
    @PostMapping("/shorten")
    public ShortenResponse shorten(@RequestBody @Valid ShortenRequest request) {
        return urlService.shorten(request);
    }

    @GetMapping("/findOne/{id}")
    public ShortenResponse get(@PathVariable long id) {
        return urlService.findById(id);
    }

    @GetMapping("/findByCode/{code}")
    public ShortenResponse get(@PathVariable String code) {
        return urlService.findByCode(code);
    }

    @GetMapping("/my-urls")
    public List<ShortenResponse> getMyUrls() {
        return urlService.getUrlsForCurrentUser();
    }

    // ---------------- Public redirect endpoint (browser click) ----------------
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
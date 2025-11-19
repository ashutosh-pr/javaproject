package com.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
// import org.springframework.http.ResponseEntity;
// import org.springframework.transaction.annotation.Transactional;

// import java.net.URI;
// import java.net.URISyntaxException;
// import java.util.Random;
// import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import com.demo.dto.ShortenRequest;
import com.demo.dto.ShortenResponse;
import com.demo.entity.UrlMapping;
import com.demo.entity.User;
import com.demo.exception.NotFoundException;
import com.demo.repository.UrlMappingRepository;
import com.demo.repository.UserRepository;
import com.demo.util.Base62;

@Service
public class UrlService {

    private static final String BASE_URL = "http://localhost:8080/"; // concrete for local use

    @Autowired
    private UrlMappingRepository repository;
    @Autowired
    private UserRepository userRepository;

    public ShortenResponse shorten(ShortenRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        UrlMapping urlEntity = new UrlMapping(Base62.randomCode(5), req.getOriginalUrl());
        urlEntity.setUser(user);
        UrlMapping saved = repository.save(urlEntity);
        String code = saved.getShortCode();
        // abc
        // localhost:8080/abc
        String shortUrl = BASE_URL + code;
        return new ShortenResponse(code, shortUrl, req.getOriginalUrl());
    }

    public ShortenResponse shorten2(ShortenRequest req) {
        UrlMapping urlEntity = new UrlMapping(Base62.randomCode(5), req.getOriginalUrl());
        UrlMapping saved = repository.save(urlEntity);
        String code = saved.getShortCode();
        // abc
        // localhost:8080/abc
        String shortUrl = BASE_URL + code;
        return new ShortenResponse(code, shortUrl, req.getOriginalUrl());
    }

    public ShortenResponse findById(long id) {

        UrlMapping urlMapping = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Short URL not found"));
        String code = urlMapping.getShortCode();
        return new ShortenResponse(code, BASE_URL + code, urlMapping.getOriginalUrl());
    }

    public ShortenResponse findByCode(String code) {
        UrlMapping urlMapping = repository.findByShortCodeAndActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Short URL not found or inactive"));
        return new ShortenResponse(code, BASE_URL + code, urlMapping.getOriginalUrl());
    }

    public List<ShortenResponse> getUrlsForCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username).orElse(null);
    List<UrlMapping> urls = repository.findAllByUser(user);
    return urls.stream()
        .map(url -> new ShortenResponse(url.getShortCode(), BASE_URL + url.getShortCode(), url.getOriginalUrl()))
        .collect(Collectors.toList());
}

public String getOriginalUrlByCode(String code) {
    UrlMapping urlMapping = repository.findByShortCodeAndActiveTrue(code)
        .orElseThrow(() -> new NotFoundException("Short URL not found or inactive"));
    return urlMapping.getOriginalUrl();
}
}
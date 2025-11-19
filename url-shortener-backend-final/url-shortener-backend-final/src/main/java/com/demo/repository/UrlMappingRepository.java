package com.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.entity.UrlMapping;
import com.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);

    // New: only active records
    Optional<UrlMapping> findByShortCodeAndActiveTrue(String shortCode);


    List<UrlMapping> findAllByUser(User user);
}

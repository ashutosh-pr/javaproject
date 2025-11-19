## Part 1: Add JWT + Security Dependencies

Update your `pom.xml` with correct versions:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>
```

---

## Part 2: Create User and Role Entities

```java
package com.demo.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // Constructors
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

@Entity
@Table(name = "users")
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Constructors
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
```

---

## Part 3: Create User and Role Repositories

```java
package com.demo.repository;

import com.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

```java
package com.demo.repository;

import com.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
```

---

## Part 4: JWT Utility Class (Updated for JJWT 0.12.5)

```java
package com.demo.security;

import com.demo.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // 512-bit secret key for HS512 (must be at least 64 characters)
    private static final String SECRET = "MyVeryLongSecretKeyForJWTGenerationThatIsAtLeast64CharactersLongForHS512Algorithm";
    private static final long EXPIRATION = 86400000; // 1 day in milliseconds

    private final SecretKey secretKey;

    public JwtUtil() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        claims.put("userId", user.getId());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            return validateToken(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
```

---

## Part 5: DTOs for Authentication

```java
package com.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

```java
package com.demo.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String message;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String username, String message) {
        this.token = token;
        this.username = username;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
```

```java
package com.demo.dto;

public class ErrorResponse {
    private String error;
    private String message;
    private int status;

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }

    // Getters and Setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
```

---

## Part 6: JWT Filter (Updated for Spring Security 6.x)

```java
package com.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                Claims claims = jwtUtil.validateToken(token);
                String username = claims.getSubject();
                
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException e) {
                logger.warn("JWT token validation failed: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## Part 7: Auth Controller with Proper Error Handling

```java
package com.demo.controller;

import com.demo.dto.AuthRequest;
import com.demo.dto.AuthResponse;
import com.demo.dto.ErrorResponse;
import com.demo.model.User;
import com.demo.repository.UserRepository;
import com.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder, 
                         JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + request.getUsername()));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid username or password", 401));
            }

            String token = jwtUtil.generateToken(user);
            AuthResponse response = new AuthResponse(token, user.getUsername(), "Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("LOGIN_FAILED", e.getMessage(), 401));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            // This endpoint requires authentication
            return ResponseEntity.ok("Profile access successful! You are authenticated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("PROFILE_ERROR", e.getMessage(), 500));
        }
    }
}
```

---

## Part 8: Security Configuration (Spring Security 6.x Compatible)

```java
package com.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // For H2 database console
                .requestMatchers("/api/shorten").hasAuthority("ROLE_USER")
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 Console
        // http.headers(headers -> headers.frameOptions().disable());
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## Part 9: Database Initialization Service

```java
package com.demo.service;

import com.demo.model.Role;
import com.demo.model.User;
import com.demo.repository.RoleRepository;
import com.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(RoleRepository roleRepository, 
                             UserRepository userRepository, 
                             PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        if (!roleRepository.existsByName("ROLE_USER")) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // Create sample users for testing
        if (!userRepository.existsByUsername("suresh")) {
            User user = new User("suresh", passwordEncoder.encode("password123"));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            user.addRole(userRole);
            userRepository.save(user);
            System.out.println("Created user: suresh with password: password123");
        }

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"));
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            admin.addRole(adminRole);
            admin.addRole(userRole);
            userRepository.save(admin);
            System.out.println("Created admin: admin with password: admin123");
        }
    }
}
```

---

## Part 10: Update URL Controller for Security

First, create the missing UrlRequest DTO:

```java
package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UrlRequest {
    @NotBlank(message = "Original URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    // Constructors
    public UrlRequest() {}

    public UrlRequest(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    // Getters and Setters
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
}
```

Now, the corrected URL Controller:

```java
package com.demo.controller;

import com.demo.dto.UrlRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UrlController {

    // Your existing URL shortening code here...

    @PostMapping("/shorten")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody UrlRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("URL shortened by user: " + currentUser);
        
        // Your existing URL shortening logic here
        // Make sure to associate the URL with the current user if needed
        
        return ResponseEntity.ok("URL shortened successfully by " + currentUser + " for URL: " + request.getOriginalUrl());
    }

    @GetMapping("/admin/urls")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUrls() {
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Admin accessing all URLs: " + currentAdmin);
        
        // Return all URL mappings - admin only feature
        return ResponseEntity.ok("All URLs accessed by admin: " + currentAdmin);
    }
}

---

## Part 11: Application Properties Configuration

Add these properties to your `application.properties`:

```properties
# Database Configuration (for H2)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.demo=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## Testing Your Implementation

### Step 1: Start Application and Check Logs
You should see:
```
Created user: suresh with password: password123
Created admin: admin with password: admin123
```

### Step 2: Test Login with Postman

**Login as Regular User:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "suresh",
    "password": "password123"
}
```

**Expected Response:**
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "suresh",
    "message": "Login successful"
}
```

### Step 3: Test Protected Endpoints

**Test URL Shortening (requires ROLE_USER):**
```http
POST http://localhost:8080/api/shorten
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
    "originalUrl": "https://flipkart.com"
}
```

**Test Admin Endpoint (requires ROLE_ADMIN):**
```http
GET http://localhost:8080/api/admin/urls
Authorization: Bearer ADMIN_JWT_TOKEN
```

---

## Real-World Example: Indian Railways Booking System

Let's create a practical example using Indian Railways context:

```java
@RestController
@RequestMapping("/api/railway")
public class RailwayController {

    @PostMapping("/book-ticket")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> bookTicket(@RequestBody BookingRequest request) {
        String passenger = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Sample booking logic
        String pnr = generatePNR();
        String message = String.format(
            "Ticket booked successfully! PNR: %s, Route: %s to %s, Passenger: %s",
            pnr, request.getFrom(), request.getTo(), passenger
        );
        
        return ResponseEntity.ok(new BookingResponse(pnr, message));
    }

    @GetMapping("/admin/all-bookings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        String admin = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Return booking statistics for admin
        return ResponseEntity.ok("All railway bookings accessed by admin: " + admin);
    }
    
    private String generatePNR() {
        return "PNR" + System.currentTimeMillis();
    }
}

class BookingRequest {
    private String from;  // e.g., "Mumbai Central"
    private String to;    // e.g., "New Delhi"
    private String date;
    private int passengers;
    
    // getters and setters
}

class BookingResponse {
    private String pnr;
    private String message;
    
    public BookingResponse(String pnr, String message) {
        this.pnr = pnr;
        this.message = message;
    }
    
    // getters and setters
}
```
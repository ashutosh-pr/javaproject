package com.demo.controller;

import com.demo.dto.AuthRequest;
import com.demo.dto.AuthResponse;
import com.demo.dto.ErrorResponse;
import com.demo.entity.Role;
import com.demo.entity.User;
import com.demo.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("USERNAME_TAKEN", "Username already exists", 400));
            }

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));

            // Assign ROLE_USER by default
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            newUser.addRole(userRole);

            userRepository.save(newUser);

            // Generate JWT token
            String token = jwtUtil.generateToken(newUser);
            AuthResponse response = new AuthResponse(token, newUser.getUsername(), "Register successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("REGISTER_FAILED", e.getMessage(), 500));
        }
    }

    // ---------------- LOGIN ----------------
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

    // ---------------- PROFILE (example protected endpoint) ----------------
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            // This endpoint requires authentication via JWT
            return ResponseEntity.ok("Profile access successful! You are authenticated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("PROFILE_ERROR", e.getMessage(), 500));
        }
    }
}




// package com.demo.controller;

// import com.demo.dto.AuthRequest;
// import com.demo.dto.AuthResponse;
// import com.demo.dto.ErrorResponse;
// import com.demo.entity.User;
// import com.demo.repository.UserRepository;
// import com.demo.security.JwtUtil;
// import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {

//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;
//     private final JwtUtil jwtUtil;

//     public AuthController(UserRepository userRepository, 
//                          PasswordEncoder passwordEncoder, 
//                          JwtUtil jwtUtil) {
//         this.userRepository = userRepository;
//         this.passwordEncoder = passwordEncoder;
//         this.jwtUtil = jwtUtil;
//     }

//     @PostMapping("/login")
//     public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
//         try {
//             User user = userRepository.findByUsername(request.getUsername())
//                     .orElseThrow(() -> new RuntimeException("User not found with username: " + request.getUsername()));

//             if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid username or password", 401));
//             }

//             String token = jwtUtil.generateToken(user);
//             AuthResponse response = new AuthResponse(token, user.getUsername(), "Login successful");
            
//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(new ErrorResponse("LOGIN_FAILED", e.getMessage(), 401));
//         }
//     }

//     @GetMapping("/profile")
//     public ResponseEntity<?> getUserProfile() {
//         try {
//             // This endpoint requires authentication
//             return ResponseEntity.ok("Profile access successful! You are authenticated.");
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ErrorResponse("PROFILE_ERROR", e.getMessage(), 500));
//         }
//     }
// }
package com.demo.service;

import com.demo.entity.Role;
import com.demo.entity.User;
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
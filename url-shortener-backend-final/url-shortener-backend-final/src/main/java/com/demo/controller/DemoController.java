package com.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    // get http://localhost:8080/greet
    @GetMapping("/greet")
    public String greet() {
        return "Hello, World!";
    }

    
}

package com.guvi.gateway.backend;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class DemoBackendController {
    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return List.of(Map.of("id", 1, "name", "Asha"), Map.of("id", 2, "name", "Rahul"));
    }
    @GetMapping("/users/{id}")
    public Map<String, Object> user(@PathVariable int id) {
        return Map.of("id", id, "name", "Demo User " + id);
    }
    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return List.of(Map.of("id", 101, "name", "Keyboard", "price", 799),
                Map.of("id", 102, "name", "Mouse", "price", 499));
    }
    @GetMapping("/orders")
    public List<Map<String, Object>> orders() {
        return List.of(Map.of("id", 5001, "status", "CREATED"), Map.of("id", 5002, "status", "SHIPPED"));
    }
    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> body) {
        return Map.of("message", "Demo order accepted", "received", body);
    }
}

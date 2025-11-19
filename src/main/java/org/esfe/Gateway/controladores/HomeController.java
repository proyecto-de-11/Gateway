package org.esfe.Gateway.controladores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la ruta raíz del Gateway
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "API Gateway - Sistema de Gestión Deportiva");
        response.put("status", "ONLINE");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
                "debug", "/debug/info",
                "services", "/debug/services",
                "health", "/debug/health",
                "auth", "/api/auth/**",
                "users", "/api/usuarios/**"
        ));
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}
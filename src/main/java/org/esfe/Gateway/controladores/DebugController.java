package org.esfe.Gateway.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para endpoints de debugging y monitoreo del Gateway
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * Lista todos los servicios registrados en Eureka
     */
    @GetMapping("/services")
    public Map<String, Object> getServices() {
        Map<String, Object> result = new HashMap<>();

        // Obtener todos los servicios registrados
        List<String> services = discoveryClient.getServices();
        result.put("totalServices", services.size());
        result.put("services", services);

        // Obtener instancias de cada servicio
        Map<String, List<ServiceInstance>> serviceInstances = new HashMap<>();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            serviceInstances.put(service, instances);
        }
        result.put("serviceInstances", serviceInstances);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    /**
     * Información básica del Gateway
     */
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "API Gateway");
        info.put("status", "RUNNING");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().toString());
        return info;
    }

    /**
     * Health check del Gateway
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "HEALTHY");
        health.put("timestamp", LocalDateTime.now());

        // Verificar conectividad con Eureka
        try {
            List<String> services = discoveryClient.getServices();
            health.put("eurekaConnection", "UP");
            health.put("registeredServices", services.size());
        } catch (Exception e) {
            health.put("eurekaConnection", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }
}
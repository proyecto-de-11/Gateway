package org.esfe.Gateway.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Obtener instancias de cada servicio con detalles completos
        Map<String, List<Map<String, Object>>> serviceInstances = new HashMap<>();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            List<Map<String, Object>> instanceDetails = instances.stream().map(instance -> {
                Map<String, Object> details = new HashMap<>();
                details.put("instanceId", instance.getInstanceId());
                details.put("host", instance.getHost());
                details.put("port", instance.getPort());
                details.put("uri", instance.getUri().toString());
                details.put("secure", instance.isSecure());
                details.put("metadata", instance.getMetadata());
                details.put("scheme", instance.getScheme());
                return details;
            }).collect(Collectors.toList());
            serviceInstances.put(service, instanceDetails);
        }
        result.put("serviceInstances", serviceInstances);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    /**
     * Obtener detalles específicos de un servicio
     */
    @GetMapping("/service/{serviceName}")
    public Map<String, Object> getServiceDetails(@PathVariable String serviceName) {
        Map<String, Object> result = new HashMap<>();

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toUpperCase());

        if (instances.isEmpty()) {
            result.put("error", "Servicio no encontrado: " + serviceName);
            result.put("availableServices", discoveryClient.getServices());
        } else {
            result.put("serviceName", serviceName);
            result.put("instanceCount", instances.size());

            List<Map<String, Object>> instanceDetails = instances.stream().map(instance -> {
                Map<String, Object> details = new HashMap<>();
                details.put("instanceId", instance.getInstanceId());
                details.put("host", instance.getHost());
                details.put("port", instance.getPort());
                details.put("uri", instance.getUri().toString());
                details.put("secure", instance.isSecure());
                details.put("metadata", instance.getMetadata());
                details.put("scheme", instance.getScheme());

                // URL completa que se usaría para hacer requests
                String baseUrl = instance.getUri().toString();
                details.put("exampleUrl", baseUrl + "/api/auth/login");

                return details;
            }).collect(Collectors.toList());

            result.put("instances", instanceDetails);
        }

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
            health.put("services", services);
        } catch (Exception e) {
            health.put("eurekaConnection", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }
}
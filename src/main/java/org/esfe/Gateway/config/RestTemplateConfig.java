package org.esfe.Gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate con balanceo de carga
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate con @LoadBalanced para resolver nombres de servicios
     * desde Eureka automáticamente
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
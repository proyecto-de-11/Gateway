package org.esfe.Gateway.config;

import org.esfe.Gateway.filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Registra el filtro JWT para validar tokens en rutas protegidas
     * NOTA: NO incluir /api/auth/** para que sea publica
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);

        // Solo aplicar a rutas protegidas (NO incluir /api/auth/*)
        registrationBean.setUrlPatterns(Arrays.asList(
                "/api/usuarios/*",
                "/api/roles/*",
                "/api/perfiles/*",
                "/api/preferencias/*",
                "/api/aceptaciones/*",
                "/api/usuario-membresias/*",
                "/api/documentoslegales/*",
                "/api/membresias/*",
                "/api/tiposdeporte/*"
        ));
        registrationBean.setOrder(1);

        return registrationBean;
    }

    /**
     * Configuracion de CORS para permitir peticiones desde diferentes origenes
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0); // CORS debe ejecutarse antes que JWT

        return bean;
    }
}
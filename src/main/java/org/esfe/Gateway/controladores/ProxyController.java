package org.esfe.Gateway.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador proxy que redirige requests a los microservicios correspondientes
 */
@RestController
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate; // RestTemplate con @LoadBalanced

    // ========== MICROSERVICIO DE AUTENTICACIÓN Y USUARIOS ==========

    // Nombre exacto del servicio como aparece en Eureka
    private static final String AUTH_SERVICE = "AUTENTIFICACION-SERVICE";

    @RequestMapping(value = "/api/auth/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyAuth(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/usuarios/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyUsuarios(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/roles/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRoles(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/perfiles/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPerfiles(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/preferencias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPreferencias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/aceptaciones/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyAceptaciones(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/usuario-membresias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyUsuarioMembresias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/documentoslegales/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyDocumentosLegales(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/membresias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyMembresias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    @RequestMapping(value = "/api/tiposdeporte/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyTiposDeporte(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest(AUTH_SERVICE, request, body);
    }

    /**
     * Método genérico para proxy de requests a microservicios
     */
    private ResponseEntity<?> proxyRequest(String serviceName, HttpServletRequest request, Object body) {
        try {
            // Construir URL usando el nombre del servicio (Eureka lo resolverá)
            String targetUrl = "http://" + serviceName + request.getRequestURI();

            if (request.getQueryString() != null) {
                targetUrl += "?" + request.getQueryString();
            }

            System.out.println("========================================");
            System.out.println("🔀 PROXY REQUEST");
            System.out.println("   Method: " + request.getMethod());
            System.out.println("   Path: " + request.getRequestURI());
            System.out.println("   Service: " + serviceName);
            System.out.println("   Target URL: " + targetUrl);
            System.out.println("========================================");

            // Copiar headers del request original
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Evitar copiar headers que pueden causar conflictos
                if (!headerName.equalsIgnoreCase("host") &&
                        !headerName.equalsIgnoreCase("content-length")) {
                    String headerValue = request.getHeader(headerName);
                    headers.set(headerName, headerValue);
                }
            }

            // Crear entidad HTTP
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            // Determinar método HTTP
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // Realizar petición al microservicio
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, method, entity, Object.class);

            System.out.println("✅ Respuesta del servicio " + serviceName + ": " + response.getStatusCode());
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error en proxy para " + serviceName + ": " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del gateway");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("service", serviceName);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
package org.esfe.Gateway.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
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
    private LoadBalancerClient loadBalancer;

    private final RestTemplate restTemplate = new RestTemplate();

    // ========== MICROSERVICIO DE AUTENTICACIÓN Y USUARIOS ==========

    @RequestMapping(value = "/api/auth/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyAuth(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/usuarios/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyUsuarios(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/roles/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRoles(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/perfiles/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPerfiles(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/preferencias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPreferencias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/aceptaciones/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyAceptaciones(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/usuario-membresias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyUsuarioMembresias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/documentoslegales/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyDocumentosLegales(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/membresias/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyMembresias(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    @RequestMapping(value = "/api/tiposdeporte/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyTiposDeporte(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("Autentificacion-service", request, body);
    }

    // ========== ESPACIOS PARA OTROS MICROSERVICIOS ==========

    // TODO: Microservicio de Canchas/Instalaciones
    /*
    @RequestMapping(value = "/api/canchas/**",
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyCanchas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("CANCHAS-SERVICE", request, body);
    }
    */

    // TODO: Microservicio de Reservas
    /*
    @RequestMapping(value = "/api/reservas/**",
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyReservas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("RESERVAS-SERVICE", request, body);
    }
    */

    // TODO: Microservicio de Partidos/Eventos
    /*
    @RequestMapping(value = "/api/partidos/**",
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPartidos(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("PARTIDOS-SERVICE", request, body);
    }
    */

    // TODO: Microservicio de Notificaciones
    /*
    @RequestMapping(value = "/api/notificaciones/**",
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyNotificaciones(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("NOTIFICACIONES-SERVICE", request, body);
    }
    */

    // TODO: Microservicio de Pagos
    /*
    @RequestMapping(value = "/api/pagos/**",
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPagos(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return proxyRequest("PAGOS-SERVICE", request, body);
    }
    */

    /**
     * Método genérico para proxy de requests a microservicios
     */
    private ResponseEntity<?> proxyRequest(String serviceName, HttpServletRequest request, Object body) {
        try {
            // Obtener instancia del servicio desde Eureka
            ServiceInstance instance = loadBalancer.choose(serviceName);
            if (instance == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Servicio " + serviceName + " no disponible");
                return ResponseEntity.status(503).body(errorResponse);
            }

            // Construir URL del microservicio
            String targetUrl = instance.getUri().toString() + request.getRequestURI();
            if (request.getQueryString() != null) {
                targetUrl += "?" + request.getQueryString();
            }

            System.out.println("🔀 Proxy: " + request.getMethod() + " " + request.getRequestURI() +
                    " -> " + serviceName + " (" + targetUrl + ")");

            // Copiar headers del request original
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.set(headerName, headerValue);
            }

            // Crear entidad HTTP
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            // Determinar método HTTP
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // Realizar petición al microservicio
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, method, entity, Object.class);

            return response;

        } catch (Exception e) {
            System.err.println("❌ Error en proxy para " + serviceName + ": " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del gateway: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
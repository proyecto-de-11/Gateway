package org.esfe.Gateway.filters;

import org.esfe.Gateway.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que valida tokens JWT en cada request
 * Se ejecuta antes de llegar a los controladores proxy
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Permitir OPTIONS para CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permitir rutas públicas sin autenticación
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt = null;

        // Verificar si hay token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // Si no hay token y la ruta requiere autenticación
        if (jwt == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token de acceso requerido");
            return;
        }

        // Validar el token
        try {
            if (!jwtUtil.validateToken(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Token inválido o expirado");
                return;
            }

            // Extraer información del usuario
            String email = jwtUtil.extractUsername(jwt);
            Long userId = jwtUtil.extractUserId(jwt);
            List<String> roles = jwtUtil.extractRoles(jwt);

            // Crear wrapper del request para agregar headers para los microservicios
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request);
            wrappedRequest.addHeader("X-User-Id", userId != null ? userId.toString() : "");
            wrappedRequest.addHeader("X-User-Email", email != null ? email : "");
            wrappedRequest.addHeader("X-User-Roles", roles != null ? String.join(",", roles) : "");

            filterChain.doFilter(wrappedRequest, response);

        } catch (Exception e) {
            System.err.println("❌ Error procesando token: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Error procesando token: " + e.getMessage());
        }
    }

    /**
     * Verifica si una ruta es pública (no requiere autenticación)
     */
    private boolean isPublicPath(String path) {
        return path.equals("/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/debug/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.equals("/error");
    }

    /**
     * Envía respuesta de error en formato JSON
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

    /**
     * Clase interna para wrapper del request
     * Permite agregar headers personalizados al request
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final java.util.Map<String, String> customHeaders = new java.util.HashMap<>();

        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.Set<String> set = new java.util.HashSet<>(customHeaders.keySet());
            java.util.Enumeration<String> e = super.getHeaderNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            return java.util.Collections.enumeration(set);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return java.util.Collections.enumeration(java.util.Collections.singletonList(headerValue));
            }
            return super.getHeaders(name);
        }
    }
}
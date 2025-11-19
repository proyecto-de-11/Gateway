package org.esfe.Gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Utilidad para validar y extraer información de tokens JWT
 * Debe usar la MISMA clave secreta que el microservicio de autenticación
 */
@Component
public class JwtUtil {

    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

    /**
     * Obtiene la clave de firma para validar tokens
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae el username (email) del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extrae los roles del usuario del token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extrae el email del token
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica si el token ha expirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida el token JWT
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("❌ Error validando token en Gateway: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el usuario tiene un rol específico
     */
    public Boolean hasRole(String token, String role) {
        try {
            List<String> roles = extractRoles(token);
            if (roles == null) return false;

            // Normalizar el rol (agregar ROLE_ si no lo tiene)
            String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return roles.contains(normalizedRole);
        } catch (Exception e) {
            return false;
        }
    }
}
package com.edullm.gateway.filter;

import com.edullm.gateway.util.FrontendProperties;
import com.edullm.gateway.util.JwtUtil;
import com.edullm.rules.RoleRulesProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private RoleRulesProperties roleRules;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private FrontendProperties frontendProperties;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("* Entrando a {}", path);

        // Login
        if (path.equals("/login-success")) {
            return handleLoginSuccessAndRedirect(exchange, chain);
        }

        // Maneja /api/auth/verify (verificación desde frontend)
        if (path.equals("/api/auth/verify")) {
            return handleVerify(exchange, chain);
        }

        // Maneja /api/auth/logout - limpia la cookie y responde sin reenviar al auth-ms
        if (path.equals("/api/auth/logout")) {
            return handleLogout(exchange);
        }
        
        // Rutas públicas (sin autenticación)
        if (roleRules.getPublicPaths().stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        
        // Rutas protegidas (requieren token válido)
        return handleProtectedRoute(exchange, chain);
    }
    
    /**
     * Maneja /login-success: valida token y redirige al frontend correspondiente
     */
    private Mono<Void> handleLoginSuccessAndRedirect(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("=== HANDLE LOGIN SUCCESS AND REDIRECT ===");
        
        // Extraer token de la cookie
        String token = extractToken(exchange);
        
        if (token == null) {
            log.warn("No se encontró token en /login-success");
            return redirectToLogin(exchange);
        }
        
        log.info("Token encontrado, validando...");
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token inválido en /login-success");
            return redirectToLogin(exchange);
        }
        
        // Extraer información del usuario
        String username = jwtUtil.extractUsername(token);
        String rol = jwtUtil.extractRol(token);
        
        log.info("Login exitoso - Usuario: {}, Rol: {}", username, rol);
        
        // Obtener URL del frontend según el rol
        String frontendUrl = frontendProperties.getUrlByRole(rol);
        String redirectUrl = frontendUrl + "/dashboard";
        
        log.info("Redirigiendo a: {}", redirectUrl);
        
        // Redirigir al frontend
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));
        return exchange.getResponse().setComplete();
    }
    
    /**
     * Maneja rutas protegidas de API
     */
    private Mono<Void> handleProtectedRoute(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange);
        
        if (token == null) {
            return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token requerido");
        }
        
        if (!jwtUtil.validateToken(token)) {
            return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token inválido");
        }
        
        String rol = jwtUtil.extractRol(token);
        
         //Revision de permisos de roles por URLs
        if (!isAuthorized(exchange.getRequest().getPath().toString(), rol)) {
            return errorResponse(exchange, HttpStatus.FORBIDDEN, "Sin permiso");
        }
        
        
        // Agregar headers y continuar
        ServerHttpRequest mutated = exchange.getRequest().mutate()
            .header("X-User-Id", String.valueOf(jwtUtil.extractIdUsuario(token)))
            .header("X-User-Role", rol)
            .header("X-Username", jwtUtil.extractUsername(token))
            .build();
        
        log.debug("Usuario autenticado: {}, Rol: {}", jwtUtil.extractUsername(token), rol);
        
        return chain.filter(exchange.mutate().request(mutated).build());
    }
    
    /**
     * Redirige a la página de login
     */
    private Mono<Void> redirectToLogin(ServerWebExchange exchange) {
        log.info("Redirigiendo a /login");
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/login"));
        return exchange.getResponse().setComplete();
    }
    
    /**
     * Extrae token de Cookie o Header
     */
    private String extractToken(ServerWebExchange exchange) {
        // Log todas las cookies para debug
        var allCookies = exchange.getRequest().getCookies();
        log.debug("Cookies recibidas: {}", allCookies.keySet());
        
        var cookies = exchange.getRequest().getCookies().getFirst("jwtToken");
        if (cookies != null) {
            log.debug("Token extraído de cookie jwtToken");
            return cookies.getValue();
        }
        
        var authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token extraído de Authorization header");
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * Verifica si el rol tiene acceso a la ruta
     */
    private boolean isAuthorized(String path, String rol) {
        var allowed = roleRules.getRoleRules().get(rol);
        return allowed != null && allowed.stream().anyMatch(p -> {
            String regex = p
                .replace(".", "\\.")       // escapar puntos literales
                .replace("**", "___GLOB___")
                .replace("*", "[^/]*")
                .replace("___GLOB___", ".*");
            return path.matches(regex);
        });
    }
    
    /**
     * Respuesta de error en JSON
     */
    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String msg) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = String.format("{\"error\":\"%s\"}", msg);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    private Mono<Void> writeJsonResponse(ServerWebExchange exchange, HttpStatus status, Map<String, Object> body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = null;
		try {
			bytes = new ObjectMapper().writeValueAsBytes(body);
		} catch (JsonProcessingException e) {
			log.warn(e.toString());
			e.printStackTrace();
		}
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    private Mono<Void> handleLogout(ServerWebExchange exchange) {
        log.info("=== LOGOUT ===");
        // Limpiar la cookie jwtToken
        exchange.getResponse().getCookies().add("jwtToken",
            ResponseCookie.from("jwtToken", "")
                .path("/")
                .maxAge(0)
                .build());
        return writeJsonResponse(exchange, HttpStatus.OK, Map.of("message", "Logout exitoso"));
    }

    private Mono<Void> handleVerify(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("=== VERIFY ===");
        String token = extractToken(exchange);
        if (token == null || !jwtUtil.validateToken(token)) {
            return writeJsonResponse(exchange, HttpStatus.UNAUTHORIZED, Map.of("authenticated", false));
        }
        String username = jwtUtil.extractUsername(token);
        String rol = jwtUtil.extractRol(token);
        Long idUsuario = jwtUtil.extractIdUsuario(token);
        Map<String, Object> body = Map.of(
            "authenticated", true,
            "username", username,
            "rol", rol,
            "idUsuario", idUsuario,
            "token", token
        );
        return writeJsonResponse(exchange, HttpStatus.OK, body);
    }
    
    
    
    @Override
    public int getOrder() {
        return -100;
    }
}
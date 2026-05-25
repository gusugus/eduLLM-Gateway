package com.edullm.gateway.filter;

import com.edullm.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Rutas que no requieren autenticación
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",//POST
        "/api/auth/forgot-password",//POST
        "/api/auth/reset-password",
        "/login",//GET
        "/forgot-password",//GET
        "/reset-password"//GET
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        log.info(">>> FILTRO JWT ejecutándose para ruta: {}", path);
        // Permitir rutas públicas
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) { 
        	log.info("Ruta pública detectada: {}", path);
            return chain.filter(exchange);
        }

        // Obtener el token del header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Validar token
            if (!jwtUtil.validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extraer claims
            String username = jwtUtil.extractUsername(token);
            Long idUsuario = jwtUtil.extractIdUsuario(token);
            String rol = jwtUtil.extractRol(token);

            log.debug("Token válido para usuario: {}, id: {}, rol: {}", username, idUsuario, rol);

            // Añadir headers con la información del usuario para los microservicios
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(idUsuario))
                    .header("X-User-Role", rol)
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // Ejecutar antes que otros filtros
    }
}
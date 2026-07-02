package com.edullm.gateway.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.frontend-proxy")
public class FrontendProxyProperties {

	private boolean enabled = false;

	/** URL pública del Gateway (ej: https://pbdqwnb8-8085.use.devtunnels.ms).
	 *  Si está vacía, se intenta obtener del Host header o X-Forwarded-Host. */
	private String publicUrl = "";

	public String getBasenameByRole(String rol) {
		switch (rol) {
			case "ROLE_ADMINISTRADOR": return "/admin";
			case "ROLE_PROFESOR": return "/profesor";
			case "ROLE_ESTUDIANTE": return "/estudiante";
			default: return "";
		}
	}
}

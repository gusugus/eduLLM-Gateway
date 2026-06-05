package com.edullm.gateway.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {
	
	private String admin = "http://localhost:8001";
	private String professor = "http://localhost:8002";
	private String student = "http://localhost:8003";
 
	 public String getUrlByRole(String role) {
	     switch (role) {
	         case "ROLE_ADMINISTRADOR": return admin;
	         case "ROLE_PROFESOR": return professor;
	         case "ROLE_ESTUDIANTE": return student;
	         default: return "";
	     }
	 }
}
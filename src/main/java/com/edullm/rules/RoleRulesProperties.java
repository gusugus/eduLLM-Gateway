package com.edullm.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Component
@ConfigurationProperties(prefix = "gateway.security")
@Data
public class RoleRulesProperties {
    private Map<String, List<String>> roleRules = new HashMap();
}
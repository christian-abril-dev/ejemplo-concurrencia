package com.abril.concurrencia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class AppConfig {
	
	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
	
	@Value("${spring.profiles.active:dev}")
	private String activeProfile;
	
	@Value("${app.version}")
	private String appVersion;
	
	@PostConstruct
	public void init() {
		log.info("========================================");
        log.info("Aplicación iniciada");
        log.info("Ambiente activo: {}", activeProfile);
        log.info("Versión: {}", appVersion);
        log.info("========================================");
	}

}

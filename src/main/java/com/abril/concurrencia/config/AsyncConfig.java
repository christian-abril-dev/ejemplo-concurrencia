package com.abril.concurrencia.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
	
	@Bean(name = "clienteExecutor")
	public Executor clienteExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); // threads que siempre estaran activos
		executor.setMaxPoolSize(20); // maximo bajo carga
		executor.setQueueCapacity(100); // tamaño de la cola de espera
		executor.setThreadNamePrefix("cliente-async-"); // nombre visible en logs
		executor.initialize();
		return executor;
	}

}

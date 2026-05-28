package com.abril.concurrencia.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
	
	private final int nucleos = Runtime.getRuntime().availableProcessors();
	
	// nuevo bean para i/o bloqueante (llamadas a servicios externos)
	@Bean(name = "clienteExecutor")
	public Executor clienteExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(nucleos * 2); // threads que siempre estaran activos, en base a los nucleos de cpu
		executor.setMaxPoolSize(nucleos * 4); // maximo bajo carga
		executor.setQueueCapacity(100); // tamaño de la cola de espera
		executor.setThreadNamePrefix("io-async-"); // nombre visible en logs
		executor.initialize();
		return executor;
	}
	
	// Pool nuevo — para tareas CPU intensivas
	@Bean(name = "cpuExecutor")
	public Executor cpuExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(nucleos + 1);
		executor.setMaxPoolSize(nucleos + 1); //no configurar más que esto. 
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("cpu-async-");
		executor.initialize();
		return executor;
	}

}

package com.abril.concurrencia.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abril.concurrencia.dto.ResumenClienteResponse;
import com.abril.concurrencia.service.ClienteService;
import com.abril.concurrencia.service.PagoService;
import com.abril.concurrencia.service.ScoreService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
	
	private final ClienteService clienteService;
	private final PagoService pagoService;
	private final ScoreService scoreService;
	private final Executor clienteExecutor;
	
	public ClienteController(ClienteService clienteService, PagoService pagoService, ScoreService scoreService,
			@Qualifier("clienteExecutor") Executor clienteExecutor) {
		this.clienteService = clienteService;
		this.pagoService = pagoService;
		this.scoreService = scoreService;
		this.clienteExecutor = clienteExecutor;
	}
	
	// Endpoint 1: en serie - lento
	@GetMapping("/{id}/resumen-serie")
	public ResumenClienteResponse resumenEnSerie(@PathVariable Long id) {
		long inicio = System.currentTimeMillis();
		
		String datos = clienteService.getDatos(id);
		String historial = pagoService.getHistorial(id);
		Integer score = scoreService.getScore(id);
		
		long tiempo = System.currentTimeMillis() - inicio;
		return new ResumenClienteResponse(datos, historial, score, tiempo);
	}
	
	// Endpoint 2: en paralelo - rápido
	@GetMapping("/{id}/resumen-paralelo")
	public ResumenClienteResponse resumenParalelo(@PathVariable Long id) {
		long inicio = System.currentTimeMillis();
		
		CompletableFuture<String> datosFuture = 
				CompletableFuture.supplyAsync(() -> clienteService.getDatos(id), clienteExecutor);
		
		CompletableFuture<String> historialFuture = 
				CompletableFuture.supplyAsync(() -> pagoService.getHistorial(id), clienteExecutor);
		
		CompletableFuture<Integer> scoreFuture = 
				CompletableFuture.supplyAsync(() -> scoreService.getScore(id), clienteExecutor)
					.exceptionally(ex -> null); // se configura si falla retorna null sin romper todo
		
		CompletableFuture.allOf(datosFuture, historialFuture, scoreFuture).join();
		
		long tiempo = System.currentTimeMillis() - inicio;
		return new ResumenClienteResponse(
			datosFuture.join(),
			historialFuture.join(),
			scoreFuture.join(),
			tiempo
		);
	}
	
	@GetMapping("/{id}/resumen-con-fallo")
	public ResumenClienteResponse resumenConFallo(@PathVariable Long id) {
		long inicio = System.currentTimeMillis();
		
		CompletableFuture<String> datosFuture =
				CompletableFuture.supplyAsync(() -> clienteService.getDatos(id), clienteExecutor);
		
		CompletableFuture<String> historialFuture =
				CompletableFuture.supplyAsync(() -> pagoService.getHistorial(id), clienteExecutor);
		
		// Se implementa para hacerlo fallar
		CompletableFuture<Integer> scoreFuture =
		        CompletableFuture.<Integer>supplyAsync(() -> {
		            throw new RuntimeException("Servicio de score no disponible");
		        }, clienteExecutor)
		        .exceptionally(ex -> {
		            System.out.println("⚠️ Score falló: " + ex.getMessage() + 
		                             " | Thread: " + Thread.currentThread().getName());
		            return null; // retorna null en lugar de explotar
		        });
		
		CompletableFuture.allOf(datosFuture, historialFuture, scoreFuture).join();
		
		long tiempo = System.currentTimeMillis() - inicio;
		
		return new ResumenClienteResponse(
		        datosFuture.join(),
		        historialFuture.join(),
		        scoreFuture.join(), // null — y el endpoint sigue funcionando
		        tiempo
		);
		
	}
	
}

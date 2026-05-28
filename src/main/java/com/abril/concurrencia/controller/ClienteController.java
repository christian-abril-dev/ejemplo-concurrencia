package com.abril.concurrencia.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abril.concurrencia.dto.ResumenClienteResponse;
import com.abril.concurrencia.service.CalculoService;
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
	private final CalculoService calculoService;
	private final Executor cpuExecutor;
	
	public ClienteController(ClienteService clienteService, PagoService pagoService, ScoreService scoreService,
			CalculoService calculoService,
			@Qualifier("clienteExecutor") Executor clienteExecutor, 
			@Qualifier("cpuExecutor") Executor cpuExecutor) {
		this.clienteService = clienteService;
		this.pagoService = pagoService;
		this.scoreService = scoreService;
		this.calculoService = calculoService;
		this.clienteExecutor = clienteExecutor;
		this.cpuExecutor = cpuExecutor;
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
	
	//Mezcla el I/O y CPU correctamente - cada tarea tiene su pool
	@GetMapping("/{id}/resumen-completo")
	public Map<String, Object> resumenCompleto(@PathVariable Long id){
		long inicio = System.currentTimeMillis();
		
		//I/O en el pool de I/O
		CompletableFuture<String> datosFuture =
				CompletableFuture.supplyAsync(() -> clienteService.getDatos(id), clienteExecutor);
		
		CompletableFuture<String> historialFuture =
				CompletableFuture.supplyAsync(() -> pagoService.getHistorial(id), clienteExecutor);
		
		//CPU en el pool de CPU
		CompletableFuture<Long> primosFuture = 
				CompletableFuture.supplyAsync(() -> calculoService.calcularPrimos(50000), cpuExecutor);
		
		CompletableFuture.allOf(datosFuture, historialFuture, primosFuture).join();
		
		long tiempo = System.currentTimeMillis() - inicio;
		
		return Map.of(
				"datos", datosFuture.join(),
				"historial", historialFuture.join(),
				"primosEncontrados", primosFuture.join(),
				"tiempoMs", tiempo
		);
	}
	
	// Endpoint de diagnóstico — muestra cuántos núcleos tiene el servidor
	@GetMapping("/diagnostico")
	public Map<String, Object> diagnostico(){
		int nucleos = Runtime.getRuntime().availableProcessors();
		return Map.of(
				"nucleos", nucleos, 
				"poolIoRecomendado", nucleos * 2,
				"poolCpuRecomendado", nucleos + 1,
				"mensaje", "Configura los pools según el tipo de tarea, no con números fijos"
		);
	}
	
}

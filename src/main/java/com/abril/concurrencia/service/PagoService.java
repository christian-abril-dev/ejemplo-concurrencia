package com.abril.concurrencia.service;

import org.springframework.stereotype.Service;

@Service
public class PagoService {
	
	public String getHistorial(Long id) {
		sleep(400);
		return "Historial: 5 pagos al día";
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}

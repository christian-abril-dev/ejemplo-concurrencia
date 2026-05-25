package com.abril.concurrencia.service;

import org.springframework.stereotype.Service;

@Service
public class ClienteService {
	
	public String getDatos(Long id) {
		sleep(300);
		return "Cliente #" + id + " - Juan Perez";
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}

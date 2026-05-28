package com.abril.concurrencia.service;

import org.springframework.stereotype.Service;

@Service
public class CalculoService {
	
	// Esta clase simula trabajo CPU intensivo — calcula números primos hasta un límite
	public long calcularPrimos(int limite) {
		System.out.println("calcularPrimos en thread: " + Thread.currentThread().getName());
		long count = 0;
        for (int i = 2; i <= limite; i++) {
            if (esPrimo(i)) count++;
        }
        return count;
	}
	
	private boolean esPrimo(int n) {
		if (n < 2) return false;
		for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
	}

}

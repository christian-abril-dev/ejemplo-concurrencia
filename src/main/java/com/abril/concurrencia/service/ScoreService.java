package com.abril.concurrencia.service;

import org.springframework.stereotype.Service;

@Service
public class ScoreService {
	
	public Integer getScore(Long id) {
		System.out.println("getScore ejecutando en thread: " + Thread.currentThread().getName());
		sleep(200);
		return 850;
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}

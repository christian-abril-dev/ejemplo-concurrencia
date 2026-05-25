package com.abril.concurrencia.dto;

public class ResumenClienteResponse {
	
	private String datos;
    private String historial;
    private Integer score;
    private long tiempoMs;
    
	public ResumenClienteResponse(String datos, String historial, Integer score, long tiempoMs) {
		this.datos = datos;
		this.historial = historial;
		this.score = score;
		this.tiempoMs = tiempoMs;
	}

	public String getDatos() {
		return datos;
	}

	public String getHistorial() {
		return historial;
	}

	public Integer getScore() {
		return score;
	}

	public long getTiempoMs() {
		return tiempoMs;
	}

}

package com.SaleForceApi.domain.entity;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class SaleForceSession {

	private String sessionId;
	private String serverUrl;

	public void setSessionInfo(SaleForceSession sessionList) {
		this.sessionId = sessionList.sessionId;
		this.serverUrl = sessionList.serverUrl;
	}
}

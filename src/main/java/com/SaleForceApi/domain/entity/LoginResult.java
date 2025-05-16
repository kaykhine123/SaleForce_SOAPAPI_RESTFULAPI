package com.SaleForceApi.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {
	private final String sessionId;
	private final String serverUrl;
}
package com.SaleForceApi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.SaleForceApi.common.LoginHelper;
import com.SaleForceApi.constants.AppConstants;
import com.SaleForceApi.domain.entity.LoginResult;
import com.SaleForceApi.domain.entity.SaleForceSession;

@Component
public class SaleForceLoginInitializer implements CommandLineRunner {

	private final LoginHelper loginService;
	private final SaleForceSession session;

	public SaleForceLoginInitializer(LoginHelper loginService, SaleForceSession session) {
		this.loginService = loginService;
		this.session = session;
	}

	@Override
	public void run(String... args) {
		login(); // if app start, once time call
	}

	@Scheduled(fixedRate = 30 * 60 * 1000) // 30 mins = 30 * 60 * 1000 milliseconds
	public void login() {
		try {
			LoginResult result = loginService.login(AppConstants.USER_NAME, AppConstants.PASSWORD_TOKEN);
			session.setSessionId(result.getSessionId());
			session.setServerUrl(result.getServerUrl());

			System.out.println("✅ Salesforce Login successful");
		} catch (Exception e) {
			System.err.println("❌ Salesforce Login failed: " + e.getMessage());
		}
	}
}

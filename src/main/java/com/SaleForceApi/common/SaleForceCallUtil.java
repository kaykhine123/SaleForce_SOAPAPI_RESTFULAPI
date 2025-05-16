package com.SaleForceApi.common;

import java.util.function.Function;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;

import com.SaleForceApi.constants.AppConstants;
import com.SaleForceApi.domain.entity.LoginResult;
import com.SaleForceApi.domain.entity.SaleForceSession;
import com.SaleForceApi.exceptions.BusinessLogicException;

@Component
public class SaleForceCallUtil {

	private final LoginHelper loginService;
	private final SaleForceSession session;

	public SaleForceCallUtil(LoginHelper loginService, SaleForceSession session) {
		this.loginService = loginService;
		this.session = session;
	}

	public <T> T callWithRetry(Function<String, T> requestFunction) throws Exception {
		try {
			return requestFunction.apply(session.getSessionId());
		} catch (BusinessLogicException e) {
			System.out.println("401 received. Refreshing session...");
			LoginResult result = loginService.login(AppConstants.USER_NAME, AppConstants.PASSWORD_TOKEN);
			session.setSessionId(result.getSessionId());
			session.setServerUrl(result.getServerUrl());

			return requestFunction.apply(session.getSessionId());
		}
	}

	public String executeWithRetry(HttpUriRequestBase request) throws Exception {
		int attempts = 0;
		while (attempts < 3) {
			try (CloseableHttpClient client = HttpClients.createDefault();
					ClassicHttpResponse response = client.execute(request)) {

				int status = response.getCode();
				String json = EntityUtils.toString(response.getEntity());

				if (status == HttpStatus.SC_UNAUTHORIZED)
					throw new BusinessLogicException("Session expired");
				if (status == HttpStatus.SC_TOO_MANY_REQUESTS) {
					System.out.println("429 Too many requests — Retrying...");
					Thread.sleep((attempts + 1) * 5000L); // backoff
					attempts++;
					continue;
				}

				if (status != 200)
					throw new RuntimeException("API failed: " + json);

				return json;

			} catch (BusinessLogicException e) {
				throw e;
			}
		}
		throw new BusinessLogicException("Too many retries (429)");
	}
}

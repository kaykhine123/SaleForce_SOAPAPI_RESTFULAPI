package com.SaleForceApi.Account.domain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRegistrationRequest {

	@NotNull(message = "Billing State {field.empty}")
	private String billingState;

	@NotNull(message = "Billing Country {field.empty}")
	private String billingCountry;

	@NotNull(message = "Name {field.empty}")
	private String name;

}
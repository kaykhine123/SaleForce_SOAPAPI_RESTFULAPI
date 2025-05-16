package com.SaleForceApi.Contact.domain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactRegistrationRequest {

	@NotNull(message = "Account Id {field.empty}")
	private String accountId;

	@NotNull(message = "FirstName {field.empty}")
	private String firstName;

	@NotNull(message = "LastName {field.empty}")
	private String lastName;

}
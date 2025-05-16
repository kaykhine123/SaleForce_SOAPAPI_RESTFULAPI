package com.SaleForceApi.Contact.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SaleForceApi.Contact.domain.ContactService;
import com.SaleForceApi.Contact.domain.entity.ContactRegistrationRequest;
import com.SaleForceApi.domain.entity.SaleForceSession;
import com.SaleForceApi.exceptions.BusinessLogicException;

@RestController
@RequestMapping("/api/salesforce/contact")
public class ContactController {

	private final SaleForceSession saleForceSession;

	private final ContactService contactService;

	public ContactController(ContactService contactService, SaleForceSession saleForceSession) {
		this.contactService = contactService;
		this.saleForceSession = saleForceSession;
	}

	@GetMapping("/session")
	public Map<String, String> getSessionInfo() {
		Map<String, String> info = new HashMap<>();
		info.put("sessionId", saleForceSession.getSessionId());
		info.put("serverUrl", saleForceSession.getServerUrl());
		return info;
	}

	@GetMapping("/objectsNameByREST")
	public List<String> getObjectsName() {
		try {
			return contactService.getAllObjectNamesByRestFul();
		} catch (Exception e) {
			throw new BusinessLogicException("Getting objects are failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnNameByREST")
	public List<String> getColumnName(@RequestParam(required = true) String objectName) {
		try {
			return contactService.getAllColumnNamesWithREST(objectName);
		} catch (Exception e) {
			throw new BusinessLogicException("Getting objects are failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnAndValueByREST")
	public List<Map<String, String>> getColumnNameAndValue(@RequestParam(required = true) String objectName) {
		try {
			return contactService.getAccountDataWithAllFields(objectName);
		} catch (Exception e) {
			throw new BusinessLogicException("Getting column and value are failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnAndValueByIdByREST")
	public List<Map<String, String>> getColumnNameAndValueById(@RequestParam(required = true) String objectName,
			@RequestParam(required = true) String id) {
		try {
			return contactService.getAccountDataWithAllFieldsById(objectName, id);
		} catch (Exception e) {
			throw new BusinessLogicException("Getting column and value are failed: " + e.getMessage());
		}
	}

	@PostMapping("/insertRowBySOAP")
	public ResponseEntity<String> insertAccountRow(@RequestBody List<ContactRegistrationRequest> accountRequest) {
		try {
			contactService.createContact(accountRequest);
			return ResponseEntity.ok("Row inserted Sucessfully.");
		} catch (Exception e) {
			throw new BusinessLogicException("Row Inserting failed: " + e.getMessage());
		}
	}
}

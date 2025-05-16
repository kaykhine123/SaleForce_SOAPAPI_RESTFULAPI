package com.SaleForceApi.Account.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SaleForceApi.Account.domain.AccountService;
import com.SaleForceApi.Account.domain.entity.AccountRegistrationRequest;
import com.SaleForceApi.domain.entity.SaleForceSession;
import com.SaleForceApi.exceptions.BusinessLogicException;

@RestController
@RequestMapping("/api/salesforce/account")
public class AccountController {
	private final AccountService accountService;
	private final SaleForceSession saleForceSession;

	public AccountController(AccountService accountService, SaleForceSession saleForceSession) {
		this.accountService = accountService;
		this.saleForceSession = saleForceSession;
	}

	@GetMapping("/session")
	public Map<String, String> getSessionInfo() {
		Map<String, String> info = new HashMap<>();
		info.put("sessionId", saleForceSession.getSessionId());
		info.put("serverUrl", saleForceSession.getServerUrl());
		return info;
	}

	@GetMapping("/objectsNameBySOAP")
	public List<String> getObjectsNameBySOAP() {
		try {
			return accountService.getAllObjectNamesBySOAP();
		} catch (Exception e) {
			throw new BusinessLogicException("Getting objects are failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnNameBySOAP")
	public List<String> getColumnName(@RequestParam(required = true) String objectName) {
		try {
			return accountService.getAllColumnNamesWithSOAP(objectName);
		} catch (Exception e) {
			throw new BusinessLogicException("Getting objects are failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnNamesAndValuesBySOAP")
	public List<Map<String, String>> columnNamesAndValuesBySOAP(@RequestParam(required = true) String objectName) {
		try {
			String filterValue = "";
			String filterKey = "";
			return accountService.getAllColumnNamesAndValuesWithSOAP(objectName, filterKey, filterValue);
		} catch (Exception e) {
			throw new BusinessLogicException("Login failed: " + e.getMessage());
		}
	}

	@GetMapping("/columnNamesAndValuesBySOAPById")
	public List<Map<String, String>> getColumnNamesAndValuesById(@RequestParam(required = true) String filterValue,
			@RequestParam(required = true) String objectName, @RequestParam(required = true) String filterKey) {
		try {
			return accountService.getAllColumnNamesAndValuesWithSOAP(objectName, filterKey, filterValue);
		} catch (Exception e) {
			throw new BusinessLogicException("Getting objects are failed: " + e.getMessage());
		}
	}

	@PostMapping("/insertRowBySOAP")
	public ResponseEntity<String> insertAccountRow(@RequestBody List<AccountRegistrationRequest> accountRequest) {
		try {
			accountService.insertAccount(accountRequest);
			return ResponseEntity.ok("Row inserted Sucessfully.");
		} catch (Exception e) {
			throw new BusinessLogicException("Row Inserting failed: " + e.getMessage());
		}
	}

	@PostMapping("/updateRowBySOAP")
	public ResponseEntity<String> updateAccountRow(@RequestBody AccountRegistrationRequest accountRequest,
			@RequestParam(required = true) String id) {
		try {
			accountService.updateAccount(accountRequest, id);
			return ResponseEntity.ok("Row Update Sucessfully.");
		} catch (Exception e) {
			throw new BusinessLogicException("Row Updating failed: " + e.getMessage());
		}
	}

	@DeleteMapping("/deleteRowBySOAP")
	public ResponseEntity<String> deleteRow(@RequestParam(required = true) String id) {
		try {
			accountService.deleteRow(id);
			return ResponseEntity.ok("Row Delete Sucessfully.");
		} catch (Exception e) {
			throw new BusinessLogicException("Row Delete failed: " + e.getMessage());
		}
	}
}

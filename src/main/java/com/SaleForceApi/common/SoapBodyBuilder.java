package com.SaleForceApi.common;

import java.util.List;

import com.SaleForceApi.Account.domain.entity.AccountRegistrationRequest;

public class SoapBodyBuilder {

	public static String buildDescribeGlobalBody(String sessionId) {
		return """
				    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                      xmlns:urn="urn:enterprise.soap.sforce.com">
				        <soapenv:Header>
				            <urn:SessionHeader>
				                <urn:sessionId>%s</urn:sessionId>
				            </urn:SessionHeader>
				        </soapenv:Header>
				        <soapenv:Body>
				            <urn:describeGlobal/>
				        </soapenv:Body>
				    </soapenv:Envelope>
				""".formatted(sessionId);
	}

	public static String buildDescribeSObjectBody(String sessionId, String objectName) {
		return """
				    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                      xmlns:urn="urn:enterprise.soap.sforce.com">
				        <soapenv:Header>
				            <urn:SessionHeader>
				                <urn:sessionId>%s</urn:sessionId>
				            </urn:SessionHeader>
				        </soapenv:Header>
				        <soapenv:Body>
				            <urn:describeSObject>
				                <urn:sObjectType>%s</urn:sObjectType>
				            </urn:describeSObject>
				        </soapenv:Body>
				    </soapenv:Envelope>
				""".formatted(sessionId, objectName);
	}

	public static String buildQueryBody(String sessionId, String soqlQuery) {
		return """
				    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                      xmlns:urn="urn:enterprise.soap.sforce.com">
				        <soapenv:Header>
				            <urn:SessionHeader>
				                <urn:sessionId>%s</urn:sessionId>
				            </urn:SessionHeader>
				        </soapenv:Header>
				        <soapenv:Body>
				            <urn:query>
				                <urn:queryString>%s</urn:queryString>
				            </urn:query>
				        </soapenv:Body>
				    </soapenv:Envelope>
				""".formatted(sessionId, soqlQuery);
	}

	public static String buildInsertMultipleAccounts(String sessionId, List<AccountRegistrationRequest> accounts) {
		StringBuilder accountsXml = new StringBuilder();

		for (AccountRegistrationRequest acc : accounts) {
			accountsXml.append("""
					    <urn:sObjects xsi:type="urn:Account"
					                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
					        <urn:Name>%s</urn:Name>
					        <urn:BillingCountry>%s</urn:BillingCountry>
					        <urn:BillingState>%s</urn:BillingState>
					    </urn:sObjects>
					""".formatted(escapeXml(acc.getName()), escapeXml(acc.getBillingCountry()),
					escapeXml(acc.getBillingState())));
		}

		return """
				    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                      xmlns:urn="urn:enterprise.soap.sforce.com">
				        <soapenv:Header>
				            <urn:SessionHeader>
				                <urn:sessionId>%s</urn:sessionId>
				            </urn:SessionHeader>
				        </soapenv:Header>
				        <soapenv:Body>
				            <urn:create>
				                %s
				            </urn:create>
				        </soapenv:Body>
				    </soapenv:Envelope>
				""".formatted(sessionId, accountsXml);
	}

	public static String buildUpdateAccountBody(String sessionId, AccountRegistrationRequest request, String id) {
		return """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                  xmlns:urn="urn:enterprise.soap.sforce.com"
				                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				    <soapenv:Header>
				        <urn:SessionHeader>
				            <urn:sessionId>%s</urn:sessionId>
				        </urn:SessionHeader>
				    </soapenv:Header>
				    <soapenv:Body>
				        <urn:update>
				            <urn:sObjects xsi:type="urn:Account">
				                <urn:Id>%s</urn:Id>
				                <urn:Name>%s</urn:Name>
				                <urn:BillingCountry>%s</urn:BillingCountry>
				                <urn:BillingState>%s</urn:BillingState>
				            </urn:sObjects>
				        </urn:update>
				    </soapenv:Body>
				</soapenv:Envelope>
				""".formatted(sessionId, escapeXml(id), escapeXml(request.getName()),
				escapeXml(request.getBillingCountry()), request.getBillingState());
	}

	public static String buildDeleteRequestBody(String sessionId, String id) {

		return """
				    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                      xmlns:urn="urn:enterprise.soap.sforce.com">
				        <soapenv:Header>
				            <urn:SessionHeader>
				                <urn:sessionId>%s</urn:sessionId>
				            </urn:SessionHeader>
				        </soapenv:Header>
				        <soapenv:Body>
				            <urn:delete>
				                <urn:ids>%s</urn:ids>
				            </urn:delete>
				        </soapenv:Body>
				    </soapenv:Envelope>
				""".formatted(sessionId, escapeXml(id));
	}

	public static String escapeXml(String input) {
		if (input == null)
			return "";
		return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}
}

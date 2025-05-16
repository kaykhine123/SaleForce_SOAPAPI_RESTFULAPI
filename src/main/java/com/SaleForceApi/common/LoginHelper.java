package com.SaleForceApi.common;

import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import com.SaleForceApi.constants.SaleForceURLs;
import com.SaleForceApi.domain.entity.LoginResult;
import com.SaleForceApi.exceptions.BusinessLogicException;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

@Service
public class LoginHelper {

	public LoginResult login(String username, String passwordWithToken) throws Exception {

		// SOAP Connection
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		// Build SOAP message
		SOAPMessage soapMessage = createLoginRequest(username, passwordWithToken);

		// Send SOAP message to server
		SOAPMessage response = soapConnection.call(soapMessage, SaleForceURLs.LOGIN_URL);

		// Get sessionId from response
		SOAPBody responseBody = response.getSOAPBody();
		if (responseBody.hasFault()) {
			throw new BusinessLogicException("SOAP Fault: " + responseBody.getFault().getFaultString());
		}

		// Parse sessionId
		NodeList nodes = responseBody.getElementsByTagName("sessionId");

		// Optionally, get serverUrl
		NodeList serverUrlNodes = responseBody.getElementsByTagName("serverUrl");

		soapConnection.close();

		return new LoginResult(nodes.item(0).getTextContent(), serverUrlNodes.item(0).getTextContent());
	}

	private SOAPMessage createLoginRequest(String username, String passwordWithToken) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();

		SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration("urn", "urn:enterprise.soap.sforce.com");

		SOAPBody body = envelope.getBody();
		SOAPElement loginElement = body.addChildElement("login", "urn");
		loginElement.addChildElement("username", "urn").addTextNode(username);
		loginElement.addChildElement("password", "urn").addTextNode(passwordWithToken);

		// Set the required SOAPAction header
		message.getMimeHeaders().addHeader("SOAPAction", "urn:enterprise.soap.sforce.com");

		message.saveChanges();
		return message;
	}
}

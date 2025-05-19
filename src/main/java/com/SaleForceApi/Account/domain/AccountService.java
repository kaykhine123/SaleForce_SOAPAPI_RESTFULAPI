package com.SaleForceApi.Account.domain;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.SaleForceApi.Account.domain.entity.AccountRegistrationRequest;
import com.SaleForceApi.common.SaleForceCallUtil;
import com.SaleForceApi.common.SoapBodyBuilder;
import com.SaleForceApi.domain.entity.SaleForceSession;
import com.SaleForceApi.exceptions.BusinessLogicException;

@Service
public class AccountService {

	@Autowired
	public SaleForceCallUtil saleForceCallUtil;

	private final SaleForceSession session;

	public AccountService(SaleForceSession session) {
		this.session = session;
	}

	public List<String> getAllObjectNamesBySOAP() throws Exception {

		return saleForceCallUtil.callWithRetry(sessionId -> {
			try {
				List<String> result = new ArrayList<>();

				String soapBody = SoapBodyBuilder.buildDescribeGlobalBody(session.getSessionId());

				HttpPost post = new HttpPost(session.getServerUrl());
				post.setHeader("Content-Type", "text/xml; charset=utf-8");
				post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
				post.setEntity(new StringEntity(soapBody));

				String responseString = saleForceCallUtil.executeWithRetry(post);

				result = parseNamesFromXml(responseString, "sobjects");

				return result;

			} catch (Exception e) {
				throw new BusinessLogicException("Getting Object Names are Failed" + e.getMessage());
			}
		});

	}

	public List<String> getAllColumnNamesWithSOAP(String objectName) throws Exception {

		return saleForceCallUtil.callWithRetry(sessionId -> {
			try {
				List<String> result = new ArrayList<>();

				String soapBody = SoapBodyBuilder.buildDescribeSObjectBody(session.getSessionId(), objectName);

				HttpPost post = new HttpPost(session.getServerUrl());
				post.setHeader("Content-Type", "text/xml; charset=utf-8");
				post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
				post.setEntity(new StringEntity(soapBody));

				String responseString = saleForceCallUtil.executeWithRetry(post);

				result = parseNamesFromXml(responseString, "fields");
				return result;
			} catch (Exception e) {
				throw new BusinessLogicException("Getting Object Names are Failed" + e.getMessage());
			}
		});

	}

	public List<Map<String, String>> getAllColumnNamesAndValuesWithSOAP(String objectName, String filterKey,
			String filterValue) throws Exception {
		return saleForceCallUtil.callWithRetry(sessionId -> {
			try {
				List<Map<String, String>> result = new ArrayList<>();
				List<String> fieldNames = getAllColumnNamesWithSOAP(objectName);
				String fieldString = String.join(", ", fieldNames);

				String soql = String.format("SELECT %s FROM %s LIMIT 100", fieldString, objectName);

				if (!filterValue.isEmpty() && !filterKey.isEmpty()) {
					soql = String.format("SELECT %s FROM %s WHERE %s = '%s' LIMIT 100", fieldString, objectName,
							filterKey, filterValue);
				}

				String soapBody = SoapBodyBuilder.buildQueryBody(session.getSessionId(), soql);

				HttpPost post = new HttpPost(session.getServerUrl());
				post.setHeader("Content-Type", "text/xml; charset=utf-8");
				post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
				post.setEntity(new StringEntity(soapBody));

				String responseString = saleForceCallUtil.executeWithRetry(post);

				result = gettingResultByList(responseString, fieldNames);

				return result; // You may parse XML to extract records

			} catch (Exception e) {
				throw new BusinessLogicException("Getting Object Names are Failed" + e.getMessage());
			}
		});

	}

	public void insertAccount(List<AccountRegistrationRequest> request) throws Exception {
		String soapBody = SoapBodyBuilder.buildInsertMultipleAccounts(session.getSessionId(), request);

		HttpPost post = new HttpPost(session.getServerUrl());
		post.setHeader("Content-Type", "text/xml; charset=utf-8");
		post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
		post.setEntity(new StringEntity(soapBody));

		String responseString = saleForceCallUtil.executeWithRetry(post);

		List<Map<String, String>> rows = new ArrayList<>();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(responseString)));

		NodeList result = doc.getElementsByTagName("result");
		if (result.getLength() > 0) {
			Element limitEl1 = (Element) result.item(0);
			String resultMessage = getTagValue(limitEl1, "message");
			String isSucess = getTagValue(limitEl1, "success");
			if (isSucess.equals("false")) {
				throw new BusinessLogicException("Inserting Account Failed.." + resultMessage);
			}
		}
	}

	public void updateAccount(AccountRegistrationRequest request, String id) throws Exception {
		String soapBody = SoapBodyBuilder.buildUpdateAccountBody(session.getSessionId(), request, id);

		HttpPost post = new HttpPost(session.getServerUrl());
		post.setHeader("Content-Type", "text/xml; charset=utf-8");
		post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
		post.setEntity(new StringEntity(soapBody));

		String responseString = saleForceCallUtil.executeWithRetry(post);

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(responseString)));

		NodeList result = doc.getElementsByTagName("result");
		if (result.getLength() > 0) {
			Element limitEl1 = (Element) result.item(0);
			String resultMessage = getTagValue(limitEl1, "message");
			String isSucess = getTagValue(limitEl1, "success");
			if (isSucess.equals("false")) {
				throw new BusinessLogicException("Update Account Failed.." + resultMessage);
			}
		}
	}

	public void deleteRow(String id) throws Exception {
		String soapBody = SoapBodyBuilder.buildDeleteRequestBody(session.getSessionId(), id);

		HttpPost post = new HttpPost(session.getServerUrl());
		post.setHeader("Content-Type", "text/xml; charset=utf-8");
		post.setHeader("SOAPAction", "urn:enterprise.soap.sforce.com");
		post.setEntity(new StringEntity(soapBody));

		String responseString = saleForceCallUtil.executeWithRetry(post);

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(responseString)));

		NodeList result = doc.getElementsByTagName("result");
		if (result.getLength() > 0) {
			Element limitEl1 = (Element) result.item(0);
			String resultMessage = getTagValue(limitEl1, "message");
			String isSucess = getTagValue(limitEl1, "success");
			if (isSucess.equals("false")) {
				throw new BusinessLogicException("Delete Account Failed.." + resultMessage);
			}
		}
	}

	private List<String> parseNamesFromXml(String xml, String keyName) throws Exception {
		List<String> objectNames = new ArrayList<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));

		// Check <limitInfo>
		checkLimitInof(doc);

		NodeList sobjects = doc.getElementsByTagName(keyName);
		for (int i = 0; i < sobjects.getLength(); i++) {
			Element sobject = (Element) sobjects.item(i);
			String name = getTagValue(sobject, "name");
			if (!name.isEmpty()) {
				objectNames.add(name);
			}
		}
		return objectNames;
	}

	public List<Map<String, String>> gettingResultByList(String respongString, List<String> fieldNames)
			throws SAXException, IOException, ParserConfigurationException {
		List<Map<String, String>> rows = new ArrayList<>();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(respongString)));

		// Check <limitInfo>
		checkLimitInof(doc);

		NodeList records = doc.getElementsByTagName("records");
		for (int i = 0; i < records.getLength(); i++) {
			Element record = (Element) records.item(i);
			Map<String, String> row = new LinkedHashMap<>();

			for (String field : fieldNames) {
				String value = getTagValue(record, "sf:" + field);
				row.put(field, value);
			}
			rows.add(row);
		}
		return rows;
	}

	public void checkLimitInof(Document doc) {
		NodeList limits = doc.getElementsByTagName("limitInfo");
		if (limits.getLength() > 0) {
			Element limitEl = (Element) limits.item(0);
			String limit = getTagValue(limitEl, "limit");
			String current = getTagValue(limitEl, "current");
			String type = getTagValue(limitEl, "type");

			System.out.printf("API Limit Info: type=%s, used=%s/%s%n", type, Integer.parseInt(current), limit);

			if (Integer.parseInt(limit) - 100 <= Integer.parseInt(current)) {
				throw new RuntimeException("API limit is nearly exhausted: " + 100 + " calls left");
			}
		}
	}

	private String getTagValue(Element parent, String tagName) {
		NodeList nodeList = parent.getElementsByTagName(tagName);
		if (nodeList.getLength() > 0 && nodeList.item(0).getTextContent() != null) {
			return nodeList.item(0).getTextContent();
		}
		return "";
	}
}

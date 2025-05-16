package com.SaleForceApi.Contact.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SaleForceApi.Contact.domain.entity.ContactRegistrationRequest;
import com.SaleForceApi.common.SaleForceCallUtil;
import com.SaleForceApi.constants.SaleForceURLs;
import com.SaleForceApi.domain.entity.SaleForceSession;
import com.SaleForceApi.exceptions.BusinessLogicException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class ContactService {

	@Autowired
	public SaleForceCallUtil saleForceCallUtil;

	private final SaleForceSession session;

	public ContactService(SaleForceSession session) {
		this.session = session;
	}

	public List<String> getAllObjectNamesByRestFul() throws Exception {
		return saleForceCallUtil.callWithRetry(sessionId -> {
			try {
				String instanceUrl = session.getServerUrl().split("/services")[0]; // ✅ Trim it down for REST usage
				String restUrl = instanceUrl + SaleForceURLs.SALEFORCE_OBJECTS_NAME_URL;
				List<String> objectNames = new ArrayList<>();

				HttpGet get = new HttpGet(restUrl);
				get.setHeader("Authorization", "Bearer " + session.getSessionId());
				get.setHeader("Accept", "application/json");

				String json = saleForceCallUtil.executeWithRetry(get);

				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				JsonArray sobjects = root.getAsJsonArray("sobjects");
				for (JsonElement el : sobjects) {
					JsonObject obj = el.getAsJsonObject();
					objectNames.add(obj.get("name").getAsString());
				}
				return objectNames;
			} catch (Exception e) {
				throw new BusinessLogicException("Getting Objects are failed" + e.getMessage());
			}
		});
	}

	public List<String> getAllColumnNamesWithREST(String objectName) throws Exception {
		return saleForceCallUtil.callWithRetry(sessionId -> {
			try {
				String instanceUrl = session.getServerUrl().split("/services")[0]; // ✅ Trim it down for REST usage
				String restUrl = instanceUrl + SaleForceURLs.SALEFORCE_COLUMN_NAME_URL;
				String getColumnNameURL = String.format(restUrl, objectName);
				List<String> columnNames = new ArrayList<>();

				HttpGet get = new HttpGet(getColumnNameURL);
				get.setHeader("Authorization", "Bearer " + session.getSessionId());
				get.setHeader("Accept", "application/json");

				String json = saleForceCallUtil.executeWithRetry(get);

				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				JsonArray fields = root.getAsJsonArray("fields");
				for (JsonElement el : fields) {
					JsonObject obj = el.getAsJsonObject();
					columnNames.add(obj.get("name").getAsString());
				}
				return columnNames;
			} catch (Exception e) {
				throw new BusinessLogicException("Getting Colunm Names are failed" + e.getMessage());
			}
		});
	}

	public List<Map<String, String>> getAccountDataWithAllFields(String objectName) throws Exception {

		List<String> fieldNames = getAllColumnNamesWithREST(objectName);

		String fieldString = String.join(", ", fieldNames);

		String soql = String.format("SELECT %s FROM %s LIMIT 100", fieldString, objectName);
		String instanceUrl = session.getServerUrl().split("/services")[0]; // ✅ Trim it down for REST usage
		String queryUrl = instanceUrl + "/services/data/v62.0/query?q="
				+ URLEncoder.encode(soql, StandardCharsets.UTF_8);

		HttpGet get = new HttpGet(queryUrl);
		get.setHeader("Authorization", "Bearer " + session.getSessionId());
		get.setHeader("Accept", "application/json");

		String json = saleForceCallUtil.executeWithRetry(get);

		// Step 3: Parse result records
		JsonObject resultRoot = JsonParser.parseString(json).getAsJsonObject();
		JsonArray records = resultRoot.getAsJsonArray("records");

		List<Map<String, String>> results = new ArrayList<>();
		for (JsonElement el : records) {
			JsonObject record = el.getAsJsonObject();
			Map<String, String> row = new LinkedHashMap<>();
			for (String field : fieldNames) {
				JsonElement fieldVal = record.get(field);
				row.put(field, fieldVal.isJsonNull() || fieldVal.isJsonObject() ? "" : fieldVal.getAsString());
			}
			results.add(row);
		}

		return results;
	}

	public List<Map<String, String>> getAccountDataWithAllFieldsById(String objectName, String id) throws Exception {

		List<String> fieldNames = getAllColumnNamesWithREST(objectName);

		String fieldString = String.join(", ", fieldNames);

		String soql = String.format("SELECT %s FROM %s WHERE Id = '%s'", fieldString, objectName, id);
		String instanceUrl = session.getServerUrl().split("/services")[0]; // ✅ Trim it down for REST usage
		String queryUrl = instanceUrl + "/services/data/v62.0/query?q="
				+ URLEncoder.encode(soql, StandardCharsets.UTF_8);

		HttpGet get = new HttpGet(queryUrl);
		get.setHeader("Authorization", "Bearer " + session.getSessionId());
		get.setHeader("Accept", "application/json");

		String json = saleForceCallUtil.executeWithRetry(get);

		// Step 3: Parse result records
		JsonObject resultRoot = JsonParser.parseString(json).getAsJsonObject();
		JsonArray records = resultRoot.getAsJsonArray("records");

		List<Map<String, String>> results = new ArrayList<>();
		for (JsonElement el : records) {
			JsonObject record = el.getAsJsonObject();
			Map<String, String> row = new LinkedHashMap<>();
			for (String field : fieldNames) {
				JsonElement fieldVal = record.get(field);
				row.put(field, fieldVal.isJsonNull() || fieldVal.isJsonObject() ? "" : fieldVal.getAsString());
			}
			results.add(row);
		}

		return results;
	}

	public void createContact(List<ContactRegistrationRequest> requests) throws Exception {
		JsonArray recordsArray = new JsonArray();
		int counter = 1;

		String instanceUrl = session.getServerUrl().split("/services")[0]; // ✅ Trim it down for REST usage
		String restUrl = instanceUrl + "/services/data/v62.0/composite/tree/Contact";

		for (ContactRegistrationRequest req : requests) {
			JsonObject rec = new JsonObject();
			JsonObject attrs = new JsonObject();
			attrs.addProperty("type", "Contact");
			attrs.addProperty("referenceId", "ref" + counter++);
			rec.add("attributes", attrs);

			if (req.getAccountId() != null)
				rec.addProperty("AccountId", req.getAccountId());
			if (req.getFirstName() != null)
				rec.addProperty("FirstName", req.getFirstName());
			if (req.getLastName() != null)
				rec.addProperty("LastName", req.getLastName());

			recordsArray.add(rec);
		}

		JsonObject body = new JsonObject();
		body.add("records", recordsArray);

		HttpPost post = new HttpPost(restUrl);
		post.setHeader("Authorization", "Bearer " + session.getSessionId());
		post.setHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(body.toString()));

		saleForceCallUtil.executeWithRetry(post);
	}
}

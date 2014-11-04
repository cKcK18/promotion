package com.ken.promotion;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PromotionRecord {

	private static final String TAG = PromotionRecord.class.getSimpleName();

	public static final String SEPARATED_STRING = ", ";

	public long id; // generate from server
	public String phoneNumber;
	public String promotion;
	public String header;
	public String secondHeader;
	public String city;
	public String division;
	public String content;
	public String tradeName;
	public String address;
	public String businessHours;

	/*
	 * the function is called by local and will pass to server.
	 */
	public PromotionRecord(String phoneNumber, String promotion, String header, String secondHeader, String city, String division,
			String content, String tradeName, String address, String businessHours) {
		this.phoneNumber = phoneNumber;
		this.promotion = promotion;
		this.header = header;
		this.secondHeader = secondHeader;
		this.city = city;
		this.division = division;
		this.content = content;
		this.tradeName = tradeName;
		this.address = address;
		this.businessHours = businessHours;
	}

	/*
	 * the function is called when server handles completed.
	 */
	public PromotionRecord(long id, String phoneNumber, String promotion, String header, String secondHeader, String city, String division,
			String content, String tradeName, String address, String businessHours) {
		this.id = id;
		this.phoneNumber = phoneNumber;
		this.promotion = promotion;
		this.header = header;
		this.secondHeader = secondHeader;
		this.city = city;
		this.division = division;
		this.content = content;
		this.tradeName = tradeName;
		this.address = address;
		this.businessHours = businessHours;
	}

	public void updateRecord(PromotionRecord updateRecord) {
		this.id = updateRecord.id;
		this.phoneNumber = updateRecord.phoneNumber;
		this.promotion = updateRecord.promotion;
		this.header = updateRecord.header;
		this.secondHeader = updateRecord.secondHeader;
		this.city = updateRecord.city;
		this.division = updateRecord.division;
		this.content = updateRecord.content;
		this.tradeName = updateRecord.tradeName;
		this.address = updateRecord.address;
		this.businessHours = updateRecord.businessHours;
	}

	public String getPostBody() {
		try {
			JSONObject object = new JSONObject();
			object.put("id", id);
			object.put("phoneNumber", phoneNumber);
			object.put("promotion", promotion);
			object.put("header", header);
			object.put("secondHeader", secondHeader);
			object.put("city", city);
			object.put("division", division);
			object.put("content", content);
			object.put("tradeName", tradeName);
			object.put("address", address);
			object.put("businessHours", businessHours);

			String result = object.toString();
			Log.d(TAG, String.format("[getPostBody] %s", result));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PromotionRecord parseJsonObject(JSONObject jsonObject) {
		try {
			// Pulling items from json object
			final long id = jsonObject.getLong("id");
			final String phoneNumber = jsonObject.getString("phoneNumber");
			final String promotion = jsonObject.getString("promotion");
			final String header = jsonObject.getString("header");
			final String secondHeader = jsonObject.getString("secondHeader");
			final String city = jsonObject.getString("city");
			final String division = jsonObject.getString("division");
			final String content = jsonObject.getString("content");
			final String tradeName = jsonObject.getString("tradeName");
			final String address = jsonObject.getString("address");
			final String businessHours = jsonObject.getString("businessHours");

			final PromotionRecord record = new PromotionRecord(id, phoneNumber, promotion, header, secondHeader, city, division, content,
					tradeName, address, businessHours);
			return record;
		} catch (JSONException e) {
			Log.w(TAG, e);
		}
		return null;
	}

	public static ArrayList<PromotionRecord> parseJsonArray(JSONArray jsonArray) {
		final ArrayList<PromotionRecord> recordList = new ArrayList<PromotionRecord>();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject object = jsonArray.getJSONObject(i);
				// Pulling items from the array
				final long id = object.getLong("id");
				final String phoneNumber = object.getString("phoneNumber");
				final String promotion = object.getString("promotion");
				final String header = object.getString("header");
				final String secondHeader = object.getString("secondHeader");
				final String city = object.getString("city");
				final String division = object.getString("division");
				final String content = object.getString("content");
				final String tradeName = object.getString("tradeName");
				final String address = object.getString("address");
				final String businessHours = object.getString("businessHours");

				recordList.add(new PromotionRecord(id, phoneNumber, promotion, header, secondHeader, city, division, content, tradeName,
						address, businessHours));
			} catch (JSONException e) {
				Log.w(TAG, e);
			}
		}
		Log.d(TAG, String.format("[parseJsonArray] record list: %d", recordList.size()));
		return recordList;
	}

	@Override
	public String toString() {
		return String.format(
				"id: %d, phone: %s, promotion: %s, header: %s/%s, city: %s%s, content: %s, tradeName: %s, address: %s, businessHours: %s",
				id, phoneNumber, promotion, header, secondHeader, city, division, content, tradeName, address, businessHours);
	}
}

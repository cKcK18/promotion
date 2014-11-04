package com.ken.promotion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ken.promotion.ConnectivityHelper.OnConnectivityChangeListener;

public class PromotionRecordManager {

	private static final String TAG = PromotionRecordManager.class.getSimpleName();

	private static final String SERVER_URL = "http://106.187.42.254:3000";
	private static final String SERVER_RECORDS_URL = SERVER_URL + "/records";
	private static final String SERVER_RECORD_BY_ID_URL = SERVER_RECORDS_URL + "/:id";

	private static final String JSON_KEY_SUCCESS = "success";
	private static final String JSON_KEY_MESSGAE = "message";
	private static final String JSON_KEY_DATA = "data";

	private static final String HTTP_RESPONSE_OK = "OK";

	private static PromotionRecordManager sManager;

	private static final Comparator<PromotionRecord> COMPARATOR_BY_DATE = new Comparator<PromotionRecord>() {
		@Override
		public int compare(PromotionRecord l, PromotionRecord r) {
			return -1;
		}
	};

	public interface OnRecordChangedListener {
		void onRecordReady();

		void onRecordChanged();
	}

	private ConnectivityHelper mConnectivity;
	private boolean mReleased;

	private ArrayList<PromotionRecord> mRecordList;
	private ArrayList<PromotionRecord> mQueueAddedRecordList;
	private HashMap<PromotionRecord, PromotionRecord> mQueueUpdateRecordList;
	private ArrayList<PromotionRecord> mQueueDeletedRecordList;
	private ArrayList<OnRecordChangedListener> mOnRecordChangedListener;

	@SuppressWarnings("unused")
	private Comparator<PromotionRecord> mComparator;

	public static PromotionRecordManager getInstance(Context context) {
		if (sManager == null) {
			sManager = new PromotionRecordManager(context);
		}
		return sManager;
	}

	private PromotionRecordManager(Context context) {
		mConnectivity = new ConnectivityHelper(context);
		mConnectivity.addOnConnectivityListener(new OnConnectivityChangeListener() {
			@Override
			public void onConnectivityChange(boolean isConnected) {
				if (isConnected) {
					flushQueue();
				}
			}
		});
		mReleased = false;
		mOnRecordChangedListener = new ArrayList<OnRecordChangedListener>();
		mComparator = COMPARATOR_BY_DATE;

		queryBookingRecord();
	}

	private void addTestingRecord(ArrayList<PromotionRecord> recordList) {
	}

	private void queryBookingRecord() {
		final String functionName = "queryBookingRecord";

		// notify server and then add all into memory if success.
		new AsyncTask<Void, Void, ArrayList<PromotionRecord>>() {
			@Override
			protected ArrayList<PromotionRecord> doInBackground(Void... params) {
				final RestClient restClient = new RestClient();
				try {
					ArrayList<PromotionRecord> recordList = null;

					final String serverUrl = SERVER_RECORDS_URL + String.format("?stylistName=%s", 123);

					final Hashtable<String, String> headers = new Hashtable<String, String>();
					headers.put("Accept", "application/json");

					final String response = restClient.get(serverUrl, null, headers);
					Log.d(TAG, String.format("[%s] response: %s", functionName, response));

					final JSONObject jsonObject = new JSONObject(response);
					final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
					final String message = jsonObject.getString(JSON_KEY_MESSGAE);

					if (success && HTTP_RESPONSE_OK.equals(message)) {
						recordList = PromotionRecord.parseJsonArray(jsonObject.getJSONArray(JSON_KEY_DATA));
						addTestingRecord(recordList);
					}

					return recordList;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<PromotionRecord> result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, dataList: %d", functionName, success, success ? result.size() : 0));
				if (success) {
					mRecordList = result;
					notifyRecordReadyObserver(functionName);
				} else {
					Log.w(TAG, String.format("[%s] fail to connect to server", functionName));
				}
			}
		}.execute();
	}

	public void addBookingRecord(final PromotionRecord newRecord) {
		final String functionName = "addBookingRecord";

		// notify server and then add into memory if success.
		new AsyncTask<Void, Void, PromotionRecord>() {
			@Override
			protected PromotionRecord doInBackground(Void... params) {
				RestClient restClient = new RestClient();
				try {
					final String stringBody = newRecord.getPostBody();

					final Hashtable<String, String> headers = new Hashtable<String, String>();
					headers.put("Accept", "application/json");
					headers.put("Content-Type", "application/json");
					// FIXME
					// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
					headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

					String response = restClient.post(SERVER_RECORDS_URL, stringBody, headers);
					Log.d(TAG, String.format("[%s] response: %s", functionName, response));

					final JSONObject jsonObject = new JSONObject(response);
					final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
					final String message = jsonObject.getString(JSON_KEY_MESSGAE);

					if (success && HTTP_RESPONSE_OK.equals(message)) {
						return PromotionRecord.parseJsonObject(jsonObject.getJSONObject(JSON_KEY_DATA));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(PromotionRecord result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, dataList: %d", functionName, success, mRecordList != null ? mRecordList.size()
						: 0));
				if (success) {
					mRecordList.add(result);
					notifyRecordChangeObserver(functionName);
				} else {
					if (mQueueAddedRecordList == null) {
						mQueueAddedRecordList = new ArrayList<PromotionRecord>();
					}
					mQueueAddedRecordList.add(result);
					Log.w(TAG, String.format("[%s] fail to connect to server, add in queue", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private void addBookingRecordByBatch() {
		if (mQueueAddedRecordList == null || mQueueAddedRecordList.size() == 0) {
			return;
		}
		final String functionName = "addBookingRecordByBatch";

		// notify server and then add into memory if success.
		new AsyncTask<Void, Void, ArrayList<PromotionRecord>>() {
			@Override
			protected ArrayList<PromotionRecord> doInBackground(Void... params) {
				ArrayList<PromotionRecord> result = new ArrayList<PromotionRecord>();
				RestClient restClient = new RestClient();
				for (PromotionRecord record : mQueueAddedRecordList) {
					try {
						final String stringBody = record.getPostBody();

						final Hashtable<String, String> headers = new Hashtable<String, String>();
						headers.put("Accept", "application/json");
						headers.put("Content-Type", "application/json");
						// FIXME
						// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
						headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

						String response = restClient.post(SERVER_RECORDS_URL, stringBody, headers);
						Log.d(TAG, String.format("[%s] response: %s", functionName, response));

						final JSONObject jsonObject = new JSONObject(response);

						final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
						final String message = jsonObject.getString(JSON_KEY_MESSGAE);

						if (success && HTTP_RESPONSE_OK.equals(message)) {
							final PromotionRecord recordFromServer = PromotionRecord.parseJsonObject(jsonObject
									.getJSONObject(JSON_KEY_DATA));
							if (recordFromServer != null) {
								result.add(recordFromServer);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (result.size() > 0) {
					return result;
				}
				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<PromotionRecord> result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, successfulList: %d", functionName, success, result != null ? result.size() : 0));
				if (success) {
					for (PromotionRecord record : result) {
						mRecordList.add(record);
						mQueueAddedRecordList.remove(record);
					}
					notifyRecordChangeObserver(functionName);
				} else {
					Log.w(TAG, String.format("[%s] fail to connect to server", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	public void updateBookingRecord(final PromotionRecord oldRecord, final PromotionRecord updateRecord) {
		final String functionName = "updateBookingRecord";

		// notify server and then update old record if success.
		new AsyncTask<Void, Void, PromotionRecord>() {
			@Override
			protected PromotionRecord doInBackground(Void... params) {
				RestClient restClient = new RestClient();
				try {
					final String stringBody = updateRecord.getPostBody();

					final Hashtable<String, String> headers = new Hashtable<String, String>();
					headers.put("Accept", "application/json");
					headers.put("Content-Type", "application/json");
					// FIXME
					// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
					headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

					String response = restClient.put(SERVER_RECORD_BY_ID_URL, stringBody, headers);
					Log.d(TAG, String.format("[%s] response: %s", functionName, response));

					final JSONObject jsonObject = new JSONObject(response);
					final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
					final String message = jsonObject.getString(JSON_KEY_MESSGAE);

					if (success && HTTP_RESPONSE_OK.equals(message)) {
						return PromotionRecord.parseJsonObject(jsonObject.getJSONObject(JSON_KEY_DATA));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(PromotionRecord result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, dataList: %d", functionName, success, mRecordList != null ? mRecordList.size()
						: 0));
				if (success) {
					oldRecord.updateRecord(result);
					notifyRecordChangeObserver(functionName);
				} else {
					if (mQueueUpdateRecordList == null) {
						mQueueUpdateRecordList = new HashMap<PromotionRecord, PromotionRecord>();
					}
					mQueueUpdateRecordList.put(oldRecord, result);
					Log.w(TAG, String.format("[%s] fail to connect to server, add in queue", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	public void updateBookingRecordByBatch() {
		if (mQueueUpdateRecordList == null || mQueueUpdateRecordList.size() == 0) {
			return;
		}
		final String functionName = "updateBookingRecordByBatch";

		// notify server and then update old record if success.
		new AsyncTask<Void, Void, HashMap<PromotionRecord, PromotionRecord>>() {
			@Override
			protected HashMap<PromotionRecord, PromotionRecord> doInBackground(Void... params) {
				HashMap<PromotionRecord, PromotionRecord> result = new HashMap<PromotionRecord, PromotionRecord>();
				RestClient restClient = new RestClient();

				for (PromotionRecord oldRecord : mQueueUpdateRecordList.keySet()) {
					try {
						final PromotionRecord updateRecord = mQueueUpdateRecordList.get(oldRecord);
						final String stringBody = updateRecord.getPostBody();

						final Hashtable<String, String> headers = new Hashtable<String, String>();
						headers.put("Accept", "application/json");
						headers.put("Content-Type", "application/json");
						// FIXME
						// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
						headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

						String response = restClient.put(SERVER_RECORD_BY_ID_URL, stringBody, headers);
						Log.d(TAG, String.format("[%s] response: %s", functionName, response));

						final JSONObject jsonObject = new JSONObject(response);
						final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
						final String message = jsonObject.getString(JSON_KEY_MESSGAE);

						if (success && HTTP_RESPONSE_OK.equals(message)) {
							final PromotionRecord recordFromServer = PromotionRecord.parseJsonObject(jsonObject
									.getJSONObject(JSON_KEY_DATA));
							if (recordFromServer != null) {
								result.put(oldRecord, recordFromServer);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (result.size() > 0) {
					return result;
				}
				return null;
			}

			@Override
			protected void onPostExecute(HashMap<PromotionRecord, PromotionRecord> result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, successfulList: %d", functionName, success, result != null ? result.size() : 0));
				if (success) {
					for (PromotionRecord oldRecord : result.keySet()) {
						final PromotionRecord updateRecord = result.get(oldRecord);
						oldRecord.updateRecord(updateRecord);
						mQueueUpdateRecordList.remove(oldRecord);
					}
					notifyRecordChangeObserver(functionName);
				} else {
					Log.w(TAG, String.format("[%s] fail to connect to server", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	public void deleteBookingRecord(final PromotionRecord deletedRecord) {
		final String functionName = "deleteBookingRecord";

		// notify server and then delete it in memory if success.
		new AsyncTask<Void, Void, PromotionRecord>() {
			@Override
			protected PromotionRecord doInBackground(Void... params) {
				RestClient restClient = new RestClient();
				try {
					// final String stringBody = deletedRecord.getPostBody();

					final Hashtable<String, String> headers = new Hashtable<String, String>();
					headers.put("Accept", "application/json");
					// FIXME
					// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
					headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

					String response = restClient.delete(SERVER_RECORD_BY_ID_URL, null, headers);
					Log.d(TAG, String.format("[%s] response: %s", functionName, response));

					final JSONObject jsonObject = new JSONObject(response);
					final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
					final String message = jsonObject.getString(JSON_KEY_MESSGAE);

					if (success && HTTP_RESPONSE_OK.equals(message)) {
						return deletedRecord;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(PromotionRecord result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, dataList: %d", functionName, success, mRecordList != null ? mRecordList.size()
						: 0));
				if (success) {
					mRecordList.remove(result);
					notifyRecordChangeObserver(functionName);
				} else {

					if (mQueueDeletedRecordList == null) {
						mQueueDeletedRecordList = new ArrayList<PromotionRecord>();
					}
					mQueueDeletedRecordList.add(result);
					Log.w(TAG, String.format("[%s] fail to connect to server, add in queue", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private void deleteBookingRecordByBatch() {
		if (mQueueDeletedRecordList == null || mQueueDeletedRecordList.size() == 0) {
			return;
		}
		final String functionName = "deleteBookingRecordByBatch";

		// notify server and then delete it in memory if success.
		new AsyncTask<Void, Void, ArrayList<PromotionRecord>>() {
			@SuppressWarnings("unused")
			@Override
			protected ArrayList<PromotionRecord> doInBackground(Void... params) {
				ArrayList<PromotionRecord> result = new ArrayList<PromotionRecord>();
				RestClient restClient = new RestClient();

				for (PromotionRecord record : mQueueDeletedRecordList) {
					try {
						// final String stringBody = deletedRecord.getPostBody();

						final Hashtable<String, String> headers = new Hashtable<String, String>();
						headers.put("Accept", "application/json");
						// FIXME
						// headers.put("X-User-token", Settings.getInstance(sContext).getToken());
						headers.put("X-User-token", "3tsTzio1sUXY_JnxMMYGpykf67L1Bo8Tiw");

						String response = restClient.delete(SERVER_RECORD_BY_ID_URL, null, headers);
						Log.d(TAG, String.format("[%s] response: %s", functionName, response));

						final JSONObject jsonObject = new JSONObject(response);
						final boolean success = jsonObject.getBoolean(JSON_KEY_SUCCESS);
						final String message = jsonObject.getString(JSON_KEY_MESSGAE);

						if (success && HTTP_RESPONSE_OK.equals(message)) {
							final PromotionRecord recordFromServer = PromotionRecord.parseJsonObject(jsonObject
									.getJSONObject(JSON_KEY_DATA));
							if (recordFromServer != null) {
								result.add(recordFromServer);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (result.size() > 0) {
					return result;
				}
				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<PromotionRecord> result) {
				if (checkInstanceHadBeenReleased(functionName)) {
					return;
				}
				final boolean success = result != null;
				Log.d(TAG, String.format("[%s] success: %b, successfulList: %d", functionName, success, result != null ? result.size() : 0));
				if (success) {
					for (PromotionRecord record : result) {
						mRecordList.remove(record);
						mQueueDeletedRecordList.remove(record);
					}
					notifyRecordChangeObserver(functionName);
				} else {
					Log.w(TAG, String.format("[%s] fail to connect to server", functionName));
				}
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private boolean checkInstanceHadBeenReleased(String from) {
		if (mReleased) {
			Log.w(TAG, String.format("[%s] the instance had been released, so can't access any data", from));
			return true;
		}
		return false;
	}

	private boolean notifyRecordReadyObserver(String from) {
		if (mOnRecordChangedListener == null) {
			Log.w(TAG, String.format("[%s] no observer to be notified", from));
			return false;
		}
		for (OnRecordChangedListener listener : mOnRecordChangedListener) {
			listener.onRecordReady();
		}
		return true;
	}

	private boolean notifyRecordChangeObserver(String from) {
		if (mOnRecordChangedListener == null) {
			Log.w(TAG, String.format("[%s] no observer to be notified", from));
			return false;
		}
		for (OnRecordChangedListener listener : mOnRecordChangedListener) {
			listener.onRecordChanged();
		}
		return true;
	}

	public void setOnRecordChangedListener(OnRecordChangedListener listener) {
		mOnRecordChangedListener.add(listener);
	}

	public void removeOnRecordChangedListener(OnRecordChangedListener listener) {
		mOnRecordChangedListener.remove(listener);
	}

	public void flushQueue() {
		addBookingRecordByBatch();
		updateBookingRecordByBatch();
		deleteBookingRecordByBatch();
	}

	public void release() {
		mReleased = true;
		if (mRecordList != null) {
			mRecordList.clear();
			mRecordList = null;
		}
		if (mQueueAddedRecordList != null) {
			mQueueAddedRecordList.clear();
			mQueueAddedRecordList = null;
		}
		if (mQueueUpdateRecordList != null) {
			mQueueUpdateRecordList.clear();
			mQueueUpdateRecordList = null;
		}
		if (mQueueDeletedRecordList != null) {
			mQueueDeletedRecordList.clear();
			mQueueDeletedRecordList = null;
		}
		if (mOnRecordChangedListener != null) {
			mOnRecordChangedListener.clear();
			mOnRecordChangedListener = null;
		}
		mComparator = null;
		mConnectivity = null;
		sManager = null;
	}
}

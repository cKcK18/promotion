package com.ken.promotion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.res.AssetFileDescriptor;

public class RestClient {

	private static final String TAG = RestClient.class.getSimpleName();

	private static final int REST_CONN_TIMEOUT = 20000;
	private static final int REST_GENERAL_TIMEOUT = 5000;
	private static final int REST_UPLOAD_TIMEOUT = 1200000;

	public static final int POST = 0;
	public static final int GET = 1;

	private Boolean mStop = false;

	public String get(String uri, String authKey, Hashtable<String, String> headers) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		InputStream is = null;
		int responseCode = 0;
		String response = null;

		try {
			// Create connection
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_GENERAL_TIMEOUT);
			if (authKey != null) {
				connection.setRequestProperty("AuthKey", authKey);
			}
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}

			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			// Get Response
			responseCode = connection.getResponseCode();
			if (responseCode >= 200 && responseCode < 300) {
				is = connection.getInputStream();
				response = getResBody(is);
			} else {
				is = connection.getErrorStream();
				response = getResBody(is);
			}
		} catch (Exception exp) {
			throw exp;
		} finally {
			if (is != null) is.close();
			if (connection != null) connection.disconnect();
		}
		return response;
	}

	private static final int REST_POST_TIMEOUT = 20000;

	public String post(String uri, String body, Hashtable<String, String> headers) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		InputStream is = null;
		int responseCode = 0;
		String response = null;

		try {
			// Create connection
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_POST_TIMEOUT);
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}

			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

			if (body != null) {
				wr.write(body.getBytes("UTF-8"));
			}
			wr.flush();
			wr.close();

			// Get Response
			responseCode = connection.getResponseCode();
			if (responseCode >= 200 && responseCode < 300) {
				is = connection.getInputStream();
				response = getResBody(is);
			} else {
				is = connection.getErrorStream();
				response = getResBody(is);
			}

		} catch (InterruptedException e) {
			throw e;
		} catch (Exception exp) {
		} finally {
			if (is != null) is.close();
			if (connection != null) connection.disconnect();
		}

		// Return json result
		return response;
	}

	public String put(String uri, String body, Hashtable<String, String> headers) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		InputStream is = null;
		int responseCode = 0;
		String response = null;

		try {
			// Create connection
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("PUT");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_POST_TIMEOUT);
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}

			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

			if (body != null) {
				wr.write(body.getBytes("UTF-8"));
			}
			wr.flush();
			wr.close();

			// Get Response
			responseCode = connection.getResponseCode();
			if (responseCode >= 200 && responseCode < 300) {
				is = connection.getInputStream();
				response = getResBody(is);
			} else {
				is = connection.getErrorStream();
				response = getResBody(is);
			}

		} catch (InterruptedException e) {
			throw e;
		} catch (Exception exp) {
		} finally {
			if (is != null) is.close();
			if (connection != null) connection.disconnect();
		}

		// Return json result
		return response;
	}

	public String delete(String uri, String body, Hashtable<String, String> headers) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		InputStream is = null;
		int responseCode = 0;
		String response = null;

		try {
			// Create connection
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("DELETE");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_POST_TIMEOUT);
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}

			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

			if (body != null) {
				wr.write(body.getBytes("UTF-8"));
			}
			wr.flush();
			wr.close();

			// Get Response
			responseCode = connection.getResponseCode();
			if (responseCode >= 200 && responseCode < 300) {
				is = connection.getInputStream();
				response = getResBody(is);
			} else {
				is = connection.getErrorStream();
				response = getResBody(is);
			}

		} catch (InterruptedException e) {
			throw e;
		} catch (Exception exp) {
		} finally {
			if (is != null) is.close();
			if (connection != null) connection.disconnect();
		}

		// Return json result
		return response;
	}

	public void uploadObject(AssetFileDescriptor assetFD, String uri, String contentType) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		FileInputStream fis = null;
		long fileLength = 0;
		long nUploadedSize = 0;

		// argument checking
		if (assetFD == null) {
			throw new Exception("null argument!");
		} else {
			fileLength = assetFD.getLength();
		}

		try {

			// Write to server
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode((int) fileLength);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_UPLOAD_TIMEOUT);
			connection.setRequestProperty("connection", "close"); // Add to solve broken-pipe issue
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", "" + fileLength);

			if (null != contentType) {
				connection.setRequestProperty("Content-Type", contentType);
			}
			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			// Transfer object
			fis = new FileInputStream(assetFD.getFileDescriptor());
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

			byte[] buffer = new byte[102400];
			int n = 0;
			while ((n = fis.read(buffer)) != -1) {
				if (mStop) {
					connection.disconnect();
					throw new InterruptedException();
				}

				wr.write(buffer, 0, n);
				wr.flush();
				nUploadedSize += n;
			}
			wr.flush();
			wr.close();
			fis.close();

			getResponse(connection);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception exp) {
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (IOException e) {
			}
			if (connection != null) connection.disconnect();
		}
	}

	private void getResponse(HttpURLConnection connection) throws Exception {
		InputStream es;
		int responseCode;

		if (connection != null) {
			responseCode = connection.getResponseCode();
			if (responseCode >= 200 && responseCode < 300) {
				// Success...
				return;
			} else {
				es = connection.getErrorStream();
				if (es != null) {
					BufferedReader rd = new BufferedReader(new InputStreamReader(es));
					StringBuffer sb = new StringBuffer();
					String line;
					while ((line = rd.readLine()) != null)
						sb.append(line);
					String errString = sb.toString();
					rd.close();
					es.close();
					throw new Exception(connection.getResponseMessage() + ":" + errString + ":" + responseCode);
				}
			}
		} else {
			throw new Exception("Null connection.");
		}
	}

	public int getOnlyResponseCode(String uri, String authKey) throws Exception {
		URL url;
		HttpURLConnection connection = null;
		InputStream is = null;
		int responseCode = 0;
		try {
			// Create connection
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setConnectTimeout(REST_CONN_TIMEOUT);
			connection.setReadTimeout(REST_GENERAL_TIMEOUT);
			if (authKey != null) {
				connection.setRequestProperty("AuthKey", authKey);
			}

			// The connection wrapper that could interrupt the connection
			restConnect(connection);

			// Get Response
			responseCode = connection.getResponseCode();
		} catch (Exception exp) {
		} finally {
			if (is != null) is.close();
			if (connection != null) connection.disconnect();
		}
		return responseCode;
	}

	private String getResBody(InputStream is) throws IOException {
		String body = null;

		if (is != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null)
				sb.append(line);
			body = sb.toString();
			rd.close();
			is.close();
		}
		return body;
	}

	private void restConnect(HttpURLConnection connection) throws Exception {

		// Waiting for response
		final ArrayBlockingQueue<Runnable> tempQ = new ArrayBlockingQueue<Runnable>(1);
		ThreadPoolExecutor tempPool = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS, tempQ);
		RestConnTask connTask = new RestConnTask(connection);
		tempPool.execute(connTask);
		tempPool.shutdown();
		while (!tempPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
			if (mStop) {
				connection.disconnect();
				throw new InterruptedException();
			}
		}

		// check exception
		Exception e = connTask.getExp();
		if (e != null) throw e;
	}

	class RestConnTask implements Runnable {
		private HttpURLConnection connection = null;
		private Exception exp = null;

		public RestConnTask(HttpURLConnection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				if (connection != null) {
					connection.connect();
				} else {
					exp = new Exception("Connection is null while trying to connect.");
				}
			} catch (Exception e) {
				exp = e;
			}
		}

		public Exception getExp() {
			return exp;
		}
	}

	public void stop() {
		mStop = true;
	}

}

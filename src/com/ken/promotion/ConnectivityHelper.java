package com.ken.promotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityHelper {

	private static final String TAG = ConnectivityHelper.class.getSimpleName();

	private Context mContext;
	private OnConnectivityChangeListener mListener;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.d(TAG, String.format("[onReceive] action: %s", action));
			if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				return;
			}
			if (mListener != null) {
				mListener.onConnectivityChange(isNetworkConnect(mContext));
			}
		}
	};

	public interface OnConnectivityChangeListener {
		void onConnectivityChange(boolean isConnected);
	}

	public ConnectivityHelper(Context context) {
		mContext = context;
		mContext.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	public void addOnConnectivityListener(OnConnectivityChangeListener listener) {
		Log.d(TAG, String.format("[addOnConnectivityListener] object: %s", listener));
		mListener = listener;
	}

	public void removeOnConnectivityListener(OnConnectivityChangeListener listener) {
		Log.d(TAG, String.format("[removeOnConnectivityListener] object: %s", listener));
		mListener = null;
	}

	public void release() {
		mListener = null;
		mContext.unregisterReceiver(mReceiver);
		mContext = null;
	}

	public static boolean isNetworkConnect(Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
}

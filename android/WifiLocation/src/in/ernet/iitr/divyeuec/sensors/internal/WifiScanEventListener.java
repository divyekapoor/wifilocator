package in.ernet.iitr.divyeuec.sensors.internal;

import in.ernet.iitr.divyeuec.sensors.WifiScanResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiScanEventListener {
	// Logging
	private static final String TAG = "WifiReceiver-DK";
	
	// Instance variables
	private ArrayList<IWifiScanResultsAvailableCallback> mCallbacks = new ArrayList<IWifiScanResultsAvailableCallback>();

	private Context mCtx;
	private WifiManager mWifiManager;

	private BroadcastReceiver mWifiEventReceiver;
	
	public void setmWifiEventReceiver(BroadcastReceiver mWifiEventReceiver) {
		this.mWifiEventReceiver = mWifiEventReceiver;
	}

	public BroadcastReceiver getmWifiEventReceiver() {
		return mWifiEventReceiver;
	}

	// Methods
	public WifiScanEventListener(Context ctx) {
		mCtx = ctx;
		mWifiManager = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
	}
	
	public boolean registerCallback(IWifiScanResultsAvailableCallback callback) {
		if(mCallbacks.contains(callback))
			return true;
		mCallbacks.add(callback);
		return true;
	}
	
	public boolean unregisterCallback(IWifiScanResultsAvailableCallback callback) {
		return mCallbacks.remove(callback);
	}
	
	public int callbackCount() {
		return mCallbacks.size();
	}
	
	public BroadcastReceiver getBroadcastReceiverInstance() {
		return mWifiEventReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				WifiScanEventListener.this.onReceive(context, intent);
			}
		};
	}
	
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive Callback called.");
		String action = intent.getAction();
		Log.i(TAG, "Action: " + action);
		
		if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			Log.i(TAG, "Scan Results Available");
			
			List<ScanResult> scanResults = mWifiManager.getScanResults();
			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			for (ScanResult scanResult : scanResults) {
				Map<String, String> data = new TreeMap<String, String>();
				data.put(WifiScanResults.KEY_BSSID, scanResult.BSSID);
				data.put(WifiScanResults.KEY_SSID, scanResult.SSID);
				data.put(WifiScanResults.KEY_FREQUENCY, Integer.toString(scanResult.frequency));
				data.put(WifiScanResults.KEY_RSSI_DBM, Integer.toString(scanResult.level));
				data.put(WifiScanResults.KEY_CAPABILITIES, scanResult.capabilities);

				dataList.add(data);
			}
			
			for (IWifiScanResultsAvailableCallback callback : mCallbacks) {
				callback.onWifiScanResultsAvailable(dataList);
			}
		} else {
			Log.e(TAG, "Bad action!!! " + action);
		}
	}
}

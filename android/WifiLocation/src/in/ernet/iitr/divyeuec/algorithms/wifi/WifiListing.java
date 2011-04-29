package in.ernet.iitr.divyeuec.algorithms.wifi;

import in.ernet.iitr.divyeuec.algorithms.IAlgorithm;
import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.ISensorCallback;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiListing extends DefaultSensorCallbacks implements IAlgorithm, ISensorCallback {
	
	private SensorLifecycleManager mSensorLifecycleManager;
	private List<Map<String, String>> mScanResults;
	private WifiManager mWifiManager;
	private List<IWifiListingUpdateCallback> mCallbacks = new ArrayList<IWifiListingUpdateCallback>();

	public WifiListing(Context ctx) {
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
		mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public void resume() {
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_WIFI);		
	}
	
	@Override
	public void pause() {
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_WIFI);
	}

	public void refresh() {
		mWifiManager.startScan();
	}
	
	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		super.onWifiScanResultsAvailable(scanResults);
		mScanResults = scanResults;
		
		notifyUpdates();
	}

	public boolean registerCallback(IWifiListingUpdateCallback callback) {
		return mCallbacks.add(callback);
	}
	
	public boolean unregisterCallback(IWifiListingUpdateCallback callback) {
		return mCallbacks.remove(callback);
	}
	
	public int callbackCount() {
		return mCallbacks.size();
	}
	
	private void notifyUpdates() {
		for(IWifiListingUpdateCallback callback : mCallbacks) {
			callback.onWifiScanListUpdate();
		}
	}

	
	
	public List<Map<String, String>> getmScanResults() {
		return mScanResults;
	}

	public void setmScanResults(List<Map<String, String>> mScanResults) {
		this.mScanResults = mScanResults;
	}

	
}

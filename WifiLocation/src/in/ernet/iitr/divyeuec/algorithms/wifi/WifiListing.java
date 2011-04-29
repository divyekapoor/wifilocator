package in.ernet.iitr.divyeuec.algorithms.wifi;

import in.ernet.iitr.divyeuec.algorithms.IAlgorithm;
import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.ISensorCallback;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiListing extends DefaultSensorCallbacks implements IAlgorithm, ISensorCallback {
	
	private SensorLifecycleManager mSensorLifecycleManager;
	private List<Map<String, String>> mScanResults;
	private WifiManager mWifiManager;

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

	
	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		super.onWifiScanResultsAvailable(scanResults);
		mScanResults = scanResults;
	}

	public void refresh() {
		mWifiManager.startScan();
	}
	
}

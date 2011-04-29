package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.wifi.IWifiListingUpdateCallback;
import in.ernet.iitr.divyeuec.algorithms.wifi.WifiListing;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;
import in.ernet.iitr.divyeuec.sensors.WifiScanResults;

import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class WifiLocatorActivity extends ListActivity implements IWifiListingUpdateCallback {

	// Menu Constants
	private static final int REFRESH_MENU_ITEM = Menu.FIRST;
	private static final String TAG = "WifiLocator-DK";
	private WifiListing mWifiListing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_locator_activity);
		
		mWifiListing = new WifiListing(this);
		mWifiListing.registerCallback(this); // Make sure we get to know when the scan results update
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWifiListing.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWifiListing.pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, REFRESH_MENU_ITEM, 0, "Refresh");
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_MENU_ITEM:
			mWifiListing.refresh();
			Toast.makeText(this, "Scan Initiated", Toast.LENGTH_SHORT).show();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void onWifiScanListUpdate(List<Map<String, String>> scanResults) {
		// This function is called from the WifiScanResultsReceiver the moment
		// Scan results are available.
		Log.i(TAG, "Scan Results Available callback called.");
		Toast.makeText(this, "Scan results available", Toast.LENGTH_LONG)
				.show();

		Log.i(TAG, "dataList created from Scan Results.");

		SimpleAdapter listDataAdapter = new SimpleAdapter(this, scanResults,
				R.layout.wifi_list_item, new String[] {
						WifiScanResults.KEY_SSID, WifiScanResults.KEY_BSSID,
						WifiScanResults.KEY_FREQUENCY,
						WifiScanResults.KEY_CAPABILITIES,
						WifiScanResults.KEY_RSSI_DBM }, new int[] { R.id.ssid,
						R.id.bssid, R.id.frequency, R.id.capabilities,
						R.id.rssi });

		Log.i(TAG, "Setting List Adapter");
		getListView().setAdapter(listDataAdapter);
	}

	@Override
	public void onWifiScanListUpdate() {
		onWifiScanListUpdate(mWifiListing.getmScanResults());
		
		// Restart scanning immediately
		mWifiListing.refresh();
	}

}
package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.wifi.IOrientedWifiListingUpdateCallback;
import in.ernet.iitr.divyeuec.algorithms.wifi.OrientedWifiListing;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  This activity is used to statically log Wifi Data at a particular location.
 */
public class WifiRecorderActivity extends Activity implements
		IOrientedWifiListingUpdateCallback {
	private static final String TAG = "WifiRecorderActivity";
	private OrientedWifiListing mOrientedWifiListing;
	private PowerManager mPowerManager;
	private WakeLock mPowerLock;
	private File mSamplesFile;
	private BufferedWriter mWriter;
	private TextView mSampleInfo;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blank);

		mOrientedWifiListing = new OrientedWifiListing(this);
		mOrientedWifiListing.registerCallback(this);

		mSampleInfo = (TextView) findViewById(R.id.sampleinfo);
		
		long now = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);

		mSamplesFile = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "samples" + File.separator + "wifi", "wifi." + now + "."
				+ cal.get(Calendar.DATE) + "-" + cal.get(Calendar.MONTH) + "-"
				+ cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.HOUR_OF_DAY)
				+ "-" + cal.get(Calendar.MINUTE) + "-"
				+ cal.get(Calendar.SECOND) + ".json");
		
		try {
			if(!mSamplesFile.createNewFile()) {
				Toast.makeText(this, "Error creating file!", Toast.LENGTH_SHORT).show();
			}
			mWriter = new BufferedWriter(new FileWriter(mSamplesFile));
			
		} catch (IOException e) {
			Toast.makeText(this, "Error creating file!", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "File could not be created!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mPowerLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				TAG);
		mPowerLock.acquire();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mOrientedWifiListing.pause();
		mPowerLock.release();
		try {
			mWriter.flush();
			mWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Error in cleanly closing down the files.", e);
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mOrientedWifiListing.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	
	@Override
	public void onWifiScanListUpdate() {
		List<Map<String, String>> scanResults = mOrientedWifiListing
				.getmScanResults();
		
		LocationFingerprint lf = new LocationFingerprint(LocationFingerprint.RAVINDRA_BHAWAN_MAP_ID, 
				new Date(System.currentTimeMillis()), 0.f, 0.f, 0.f, scanResults, "test");
		
		try {
			String sample = lf.getmFingerprintData().toString();
			mWriter.write(sample);
			mWriter.write('\n');
			mSampleInfo.setText(sample);
			Toast.makeText(this, "Sample written", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(TAG, "Write to sdcard failed!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		mOrientedWifiListing.refresh();
	}
	

	@Override
	public void onAngleUpdate() {
		// TODO Auto-generated method stub

	}
}
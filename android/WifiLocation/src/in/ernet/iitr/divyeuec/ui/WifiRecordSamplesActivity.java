package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.wifi.IOrientedWifiListingUpdateCallback;
import in.ernet.iitr.divyeuec.algorithms.wifi.OrientedWifiListing;
import in.ernet.iitr.divyeuec.sensors.WifiScanResults;
import in.iitr.ernet.divyeuec.db.FileDB;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.db.PersistenceFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This Activity is used to record data corresponding to a particular location.
 * The X, Y and SampleName parameters need to be supplied by the calling activity.
 * This is used for systematically surveying an area.  
 * 
 */
public class WifiRecordSamplesActivity extends Activity implements IOrientedWifiListingUpdateCallback {
	
	private static final String TAG = "WifiRecordSamplesActivity";
	
	// The launching activity should provide certain data as part of the launch intent
	// Includes sample name, X, Y 
	
	
	private TextView mTxtAngle;
	private Button mBtnRecordSample;
	private OrientedWifiListing mOrientedWifiListing;
	private String mSampleName;
	private float mSampleX;
	private float mSampleY;
	private boolean mRecordSample = false;
	
	private TextView mSampleInfo;

	private TextView mSampleValues;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_record_samples_activity);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		
		mOrientedWifiListing = new OrientedWifiListing(this);
		mOrientedWifiListing.registerCallback(this);
		mOrientedWifiListing.registerAngleUpdateCallback(this);
		
		Intent launchIntent = getIntent();
		mSampleName = launchIntent.getStringExtra(LocationFingerprint.KEY_SAMPLE_NAME);
		mSampleName = mSampleName == null? "default_sample" : mSampleName;
		mSampleX = launchIntent.getFloatExtra(LocationFingerprint.KEY_SAMPLE_X, 0.f);
		mSampleY = launchIntent.getFloatExtra(LocationFingerprint.KEY_SAMPLE_Y, 0.f);
		
		
		mSampleInfo = (TextView) findViewById(R.id.sample_information);
		mSampleInfo.setText(mSampleName + "\n" + "(" + mSampleX + "," + mSampleY + ")");
		mSampleValues = (TextView) findViewById(R.id.sample_values);
		mTxtAngle = (TextView) findViewById(R.id.txt_angle);
		mBtnRecordSample = (Button) findViewById(R.id.btn_record_sample);
		
		mBtnRecordSample.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// The sample will be written in the onWifiScanListUpdate() callback
				// and the mRecordSample will be reset to false.
				// This is to prevent spurious sample writes as the Wifi Subsystem
				// can also issue Wifi Scans of it's own accord, whose results will
				// be seen by us. Occasionally, this might cause the detection of
				// a previously (subsystem) initiated scan to be recorded as the 
				// sample value, but it is assumed that the scan would have been initiated
				// in a period of interest and that the race condition doesn't matter.
				mRecordSample = true;
				mOrientedWifiListing.refresh();
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mOrientedWifiListing.resume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mOrientedWifiListing.pause();
	}

	@Override
	public void onWifiScanListUpdate() {
		if(mRecordSample) { // Filter out system initiated scans
			mRecordSample = false; 
			float angle = mOrientedWifiListing.getAngle();
			Date now = new Date(System.currentTimeMillis());
			List<Map<String, String>> scanResults = mOrientedWifiListing.getmScanResults();
			
			LocationFingerprint fingerprint = persistSample(angle, now, scanResults);
			
			mSampleValues.setText(fingerprint.toString());
			Toast.makeText(this, "Sample file written!", Toast.LENGTH_SHORT).show();
		}
		
	}

	private LocationFingerprint persistSample(float angle, Date now,
			List<Map<String, String>> scanResults) {
		LocationFingerprint locationFingerprint = new LocationFingerprint(LocationFingerprint.RAVINDRA_BHAWAN_MAP_ID, now, angle, mSampleX, mSampleY, scanResults, mSampleName);
		if(!PersistenceFactory.getInstance(this).persist(locationFingerprint)) {
			Toast.makeText(this, "Persistence failed!", Toast.LENGTH_SHORT).show();
			return null;
		}
		return locationFingerprint;
	}

	@Override
	public void onAngleUpdate() {
		mTxtAngle.setText("" + mOrientedWifiListing.getAngle());
	}
	
	
}

package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.DeadReckoning;
import in.ernet.iitr.divyeuec.algorithms.IReckoningMethod;
import in.ernet.iitr.divyeuec.algorithms.ParticleFilteredReckoning;
import in.ernet.iitr.divyeuec.algorithms.WifiSnappedDeadReckoning;
import in.ernet.iitr.divyeuec.algorithms.wifi.distance.ClampedOrientationPenalizedDistanceAlgorithm;
import in.ernet.iitr.divyeuec.algorithms.wifi.distance.ClampedRSSIDistanceAlgorithm;
import in.ernet.iitr.divyeuec.algorithms.wifi.distance.OrientedRSSIDistanceAlgorithm;
import in.ernet.iitr.divyeuec.algorithms.wifi.distance.RSSIDistanceAlgorithm;
import in.ernet.iitr.divyeuec.ui.views.SampleMapView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

public class DeadReckoningActivity extends Activity {

	private static final String MAP_POINT = "MapPoint";
	private static final String KEY_QR_TYPE = "Type";
	private static final int DEAD_RECKONING_TRAINING_CONSTANT = Menu.FIRST;
	private static final int DEAD_RECKONING_RESTART = Menu.FIRST + 1;
	private static final int DEAD_RECKONING_STARTPOS_QRCODE = Menu.FIRST + 2;
	private static final int DEAD_RECKONING_LOG_STEP_DATA = Menu.FIRST + 3;
	private static final int DEAD_RECKONING_STEP_SIZE_ESTIMATE = Menu.FIRST + 4;
	private static final int DEAD_RECKONING_LOG_PATH = Menu.FIRST + 5;
	
	private static final String TAG = "DeadReckoningActivity";
	
	public static final String KEY_RECKONING_METHOD = "KeyReckoningMethod";

	public static final int METHOD_DEAD_RECKONING = 1;
	public static final int METHOD_WIFI_SNAPPED_DEAD_RECKONING = 2;
	public static final int METHOD_ORIENTED_WIFI_SNAPPED_DEAD_RECKONING = 3;
	public static final int METHOD_WIFI_CLAMPED_MATCH_DEAD_RECKONING = 4;
	public static final int METHOD_WIFI_CLAMPED_ORIENTATION_PENALIZED_DEAD_RECKONING = 5;
	public static final int METHOD_PARTICLE_FILTERED_RECKONING = 6;
	
	
	// Constants for QRCode data
	private static final String KEY_VERSION = "Version";
	private static final int MIN_QRCODE_VERSION = 0x1;
	
	private WebView mWebView;
	private IReckoningMethod mDeadReckoning;
	private WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dead_reckoning_activity);
		
		int method = getIntent().getIntExtra(KEY_RECKONING_METHOD, METHOD_DEAD_RECKONING);
		Log.i(TAG, "Starting DeadReckoningActivity with reckoning method: " + method);
		switch(method) {
		case METHOD_WIFI_SNAPPED_DEAD_RECKONING:
			mDeadReckoning = new WifiSnappedDeadReckoning(this, new RSSIDistanceAlgorithm());
			break;
			
		case METHOD_ORIENTED_WIFI_SNAPPED_DEAD_RECKONING:
			mDeadReckoning = new WifiSnappedDeadReckoning(this, new OrientedRSSIDistanceAlgorithm());
			break;
		
		case METHOD_WIFI_CLAMPED_MATCH_DEAD_RECKONING:
			mDeadReckoning = new WifiSnappedDeadReckoning(this, new ClampedRSSIDistanceAlgorithm());
			break;
			
		case METHOD_WIFI_CLAMPED_ORIENTATION_PENALIZED_DEAD_RECKONING:
			mDeadReckoning = new WifiSnappedDeadReckoning(this, new ClampedOrientationPenalizedDistanceAlgorithm());
			break;
			
		case METHOD_PARTICLE_FILTERED_RECKONING:
			mDeadReckoning = new ParticleFilteredReckoning(this);
			break;
			
		case METHOD_DEAD_RECKONING:	
		default:
			mDeadReckoning = new DeadReckoning(this);
			break;
		}
		
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(mDeadReckoning, "sensorData");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, DEAD_RECKONING_TRAINING_CONSTANT, 0, "Modify Training Constant");
		menu.add(0, DEAD_RECKONING_RESTART, 0, "Restart Dead Reckoning");
		menu.add(0, DEAD_RECKONING_STARTPOS_QRCODE, 0, "Scan Location QRCode");
		if(mDeadReckoning.isLogging()) {
			menu.add(0, DEAD_RECKONING_LOG_STEP_DATA, 0, "Stop Step Path Logging");
		} else {
			menu.add(0, DEAD_RECKONING_LOG_STEP_DATA, 0, "Start Step Path Logging");
		}
		menu.add(0, DEAD_RECKONING_STEP_SIZE_ESTIMATE, 0, "Estimate Step Size");
		menu.add(0, DEAD_RECKONING_LOG_PATH, 0, "Log current displayed path");
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DEAD_RECKONING_TRAINING_CONSTANT:
			Intent launchIntent = new Intent(this, DeadReckoningTrainingActivity.class);
			launchIntent.putExtra(DeadReckoningTrainingActivity.KEY_THRESHOLD_VALUE, mDeadReckoning.getAccelThreshold());
			launchIntent.putExtra(DeadReckoningTrainingActivity.KEY_TRAINING_VALUE, mDeadReckoning.getTrainingConstant());
			startActivityForResult(launchIntent , DEAD_RECKONING_TRAINING_CONSTANT);
			break;
		case DEAD_RECKONING_RESTART:
			mDeadReckoning.restart();
			mWebView.loadUrl("file:///android_asset/deadReckoning.html"); // Refresh the page.
			break;
		case DEAD_RECKONING_STARTPOS_QRCODE:
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
	        intent.setPackage("com.google.zxing.client.android");
	        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	        startActivityForResult(intent, DEAD_RECKONING_STARTPOS_QRCODE);
			break;
			
		case DEAD_RECKONING_LOG_STEP_DATA:
			if(mDeadReckoning.isLogging()) {
				mDeadReckoning.stopLogging();
				Toast.makeText(this, "Step logging stopped", Toast.LENGTH_SHORT).show();
			} else {
				mDeadReckoning.startLogging();
				Toast.makeText(this, "Step logging started", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case DEAD_RECKONING_STEP_SIZE_ESTIMATE:
			Intent scanIntent = new Intent("com.google.zxing.client.android.SCAN");
	        scanIntent.setPackage("com.google.zxing.client.android");
	        scanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	        startActivityForResult(scanIntent, DEAD_RECKONING_STEP_SIZE_ESTIMATE);
			break;
		case DEAD_RECKONING_LOG_PATH:
			Date now = new Date(System.currentTimeMillis());
			try {
				FileWriter pathLoggingFile = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "drPathLog." + DateFormat.format("yyyy-MM-dd-kk-mm-ss", now) + ".csv"));
				ArrayList<float[]> path = mDeadReckoning.getmPath();
				for(float[] p : path) {
					pathLoggingFile.write(p[0] + "," + p[1] + "\n");
				}
				pathLoggingFile.flush();
				pathLoggingFile.close();
				Toast.makeText(this, "Path logged successfully!", Toast.LENGTH_SHORT).show();
			} catch(IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Couldn't log Path to file!", e);
				throw new RuntimeException(e);
			}
			break;
		default:
			throw new RuntimeException("Invalid Menu Option!");
		}
		boolean result = super.onOptionsItemSelected(item);
		return result;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
		mDeadReckoning.resume();
		mWebView.loadUrl("file:///android_asset/deadReckoning.html");
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
		mDeadReckoning.pause();
		mWebView.loadUrl("file:///android_asset/blank.html");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case DEAD_RECKONING_TRAINING_CONSTANT:
				switch(resultCode) {
					case RESULT_OK:
						// The DeadReckoningTrainingActivity returns the new values selected
						// using sliders.
						mDeadReckoning.setTrainingConstant(data.getFloatExtra(DeadReckoningTrainingActivity.KEY_TRAINING_VALUE, DeadReckoning.DEFAULT_TRAINING_CONSTANT/1000.f));
						mDeadReckoning.setAccelThreshold(data.getFloatExtra(DeadReckoningTrainingActivity.KEY_THRESHOLD_VALUE, DeadReckoning.DEFAULT_ACCEL_THRESHOLD/1000.f));
						break;
					default:
						throw new RuntimeException("Unexpected Activity Return value!");
				}
				break;
				
			case DEAD_RECKONING_STEP_SIZE_ESTIMATE:
			case DEAD_RECKONING_STARTPOS_QRCODE:
				switch(resultCode) {
				case RESULT_OK:
					String contents = data.getStringExtra("SCAN_RESULT");
		            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
		            Toast.makeText(this, "QRCode: " + contents + " format: " + format, Toast.LENGTH_LONG).show();
		            if(format.equals("QR_CODE")) {
		            	try {
		            		JSONObject scanQRCode = new JSONObject(contents);
		            		if(scanQRCode.has(KEY_VERSION) && scanQRCode.getInt(KEY_VERSION) >= MIN_QRCODE_VERSION && scanQRCode.has(KEY_QR_TYPE) && scanQRCode.getString(KEY_QR_TYPE).equalsIgnoreCase(MAP_POINT)) {
		            			String mapURL = scanQRCode.getString("MapURL");
		            			float x = (float)scanQRCode.getDouble("X");
		            			float y = (float)scanQRCode.getDouble("Y");
		            			float[] location = mDeadReckoning.getLocation();
		            			
		            			if(requestCode == DEAD_RECKONING_STEP_SIZE_ESTIMATE) {
		            				int numSteps = mDeadReckoning.getStepCount();
		            				double actualDistance = Math.sqrt(Math.pow(x - mDeadReckoning.getmStartX(), 2) + Math.pow(y - mDeadReckoning.getmStartY(), 2));
		            				double estimatedDistance = Math.sqrt(Math.pow(location[0] - mDeadReckoning.getmStartX(), 2) + Math.pow(location[1] - mDeadReckoning.getmStartY(), 2));
		            				double distancePerStep = (actualDistance/numSteps);
		            				double trainingConstant = mDeadReckoning.getTrainingConstant() * actualDistance/estimatedDistance;
		            				
		            				Date now = new Date(System.currentTimeMillis());
		            				try {
		            					FileWriter stepDistance = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "stepDistance." + DateFormat.format("yyyy-MM-dd-kk-mm-ss", now)));
			            				stepDistance.write("" + now.getTime() + "," + + numSteps + "," + actualDistance + "," + estimatedDistance + "," + distancePerStep + "," + trainingConstant + "\n");
			            				stepDistance.flush();
										stepDistance.close();
									} catch (IOException e) {
										Log.e(TAG, "Writing to stepDistance file failed!", e);
										e.printStackTrace();
										throw new RuntimeException(e);
									}
		            			} else if(requestCode == DEAD_RECKONING_STARTPOS_QRCODE) {
			            			// TODO Remove this hack
			            			if(true || mapURL.equalsIgnoreCase("http://www.iitr.ernet.in/maps/floor3.png")) {
			            				mDeadReckoning.restart();
			            				mDeadReckoning.setStartPos(x, y);
			            				mDeadReckoning.setLocation(x, y);
			            			}
		            			}
		            		} else {
		            			Log.i(TAG, "Missing some pre-conditions!");
		            			Toast.makeText(this, "The QRCode that you've scanned doesn't seem to be of the correct type. Please recheck and scan again", Toast.LENGTH_LONG).show();		            			
		            		}
		            	} catch(JSONException e) {
		            		Log.e(TAG, "JsonException!!!", e);
		            		Toast.makeText(this, "The QRCode that you've scanned doesn't seem to be of the correct type. Please recheck and scan again", Toast.LENGTH_LONG).show();
		            	}
		            } else {
		            	Toast.makeText(this, "Invalid QRCODE format!", Toast.LENGTH_LONG).show();
		            }
		            
		            // Handle successful scan
					break;
				case RESULT_CANCELED:
					Toast.makeText(this, "QRCode selection cancelled.", Toast.LENGTH_LONG).show();
					// Cancelled, do nothing.
					break;
				}
				break;
			default:
				throw new RuntimeException("Unexpected request code!");
		}
	}
	
	public void onSensorUpdate(float[] accel, float[] velocity,
			float[] displacement, float[] angularVelocity, float[] angles) {
		return;
	}
}

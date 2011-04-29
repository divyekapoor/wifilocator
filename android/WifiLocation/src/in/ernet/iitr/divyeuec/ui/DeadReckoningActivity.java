package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.wifi.WifiListing;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;
import in.ernet.iitr.divyeuec.sensors.internal.HWSensorEventListener;
import in.ernet.iitr.divyeuec.sensors.internal.IHWSensorEventCallback;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.webkit.WebView;

public class DeadReckoningActivity extends Activity {

	private static final String KEY_DEAD_RECKONING_SENSOR_STATE = "DeadReckoningSensorState";
	private static final int UI_UPDATE_FREQUENCY = 500; // in samples

	private SensorManager mSensorManager;
	private HWSensorEventListener mSensorEventListener;
	/*
	 * private TextView[] mAccelViews = new TextView[3]; private TextView[]
	 * mVelocityViews = new TextView[3]; private TextView[] mDisplacementViews =
	 * new TextView[3]; private TextView[] mAngularVelocityViews = new
	 * TextView[3]; private TextView[] mAngleViews = new TextView[3];
	 */
	private int mNumUpdates = 0;
	private WebView mWebView;
	private WifiListing mWifiListing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dead_reckoning_activity);

		mWifiListing = new WifiListing(this);

		// On after pause, the sensor event listener will have to be recreated
		// as the original values would have changed significantly while the
		// phone was asleep.
		// mSensorEventListener = savedInstanceState != null &&
		// savedInstanceState.containsKey(KEY_DEAD_RECKONING_SENSOR_STATE)? new
		// DeadReckoningSensorsListener(this,
		// savedInstanceState.getBundle(KEY_DEAD_RECKONING_SENSOR_STATE)) : new
		// DeadReckoningSensorsListener(this);
		

		/*
		 * 
		 * int[] accelViewIds = new int[] { R.id.accel0, R.id.accel1,
		 * R.id.accel2 }; int[] velocityViewIds = new int[] { R.id.velocity0,
		 * R.id.velocity1,R.id.velocity2 }; int[] displacementViewIds = new
		 * int[] { R.id.displacement0, R.id.displacement1, R.id.displacement2 };
		 * int[] angularVelocityViewIds = new int[] {
		 * R.id.angularVelocity0,R.id.angularVelocity1, R.id.angularVelocity2 };
		 * int[] angleViewIds = new int[] { R.id.angle0, R.id.angle1,
		 * R.id.angle2 }; for (int i = 0; i < 3; ++i) { mAccelViews[i] =
		 * (TextView) findViewById(accelViewIds[i]); mVelocityViews[i] =
		 * (TextView) findViewById(velocityViewIds[i]); mDisplacementViews[i] =
		 * (TextView) findViewById(displacementViewIds[i]);
		 * mAngularVelocityViews[i] = (TextView)
		 * findViewById(angularVelocityViewIds[i]); mAngleViews[i] = (TextView)
		 * findViewById(angleViewIds[i]); }
		 */

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(mSensorEventListener, "sensorData");
		mWebView.loadUrl("file:///android_asset/index.html");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Serialize the sensor event listener as it contains the state data -
		// angles, velocities, displacements etc.
		// TODO: Mark this data as invalid if the device goes off to sleep for
		// an extended period.
		outState.putBundle(KEY_DEAD_RECKONING_SENSOR_STATE,
				mSensorEventListener.serialize());
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWifiListing.resume();
		mWebView.loadUrl("file:///android_asset/index.html");

	}

	@Override
	protected void onPause() {
		super.onPause();
		mWebView.loadUrl("file:///android_asset/blank.html");
		mWifiListing.pause();
	}

	
	public void onSensorUpdate(float[] accel, float[] velocity,
			float[] displacement, float[] angularVelocity, float[] angles) {
		return;

		// Sensors are updated, but update UI sparingly
		/*
		 * if (mNumUpdates == 0) {
		 * 
		 * // Assign to local variables as accessing a field is a very //
		 * expensive operation TextView[] accelViews = mAccelViews; TextView[]
		 * velocityViews = mVelocityViews; TextView[] displacementViews =
		 * mDisplacementViews; TextView[] angularVelocityViews =
		 * mAngularVelocityViews; TextView[] angleViews = mAngleViews;
		 * 
		 * // Use local variables to update the text views for (int i = 0; i <
		 * 3; ++i) { accelViews[i].setText(accel[i] + " ");
		 * velocityViews[i].setText(velocity[i] + " ");
		 * displacementViews[i].setText(displacement[i] + " ");
		 * angularVelocityViews[i].setText(angularVelocity[i] + " ");
		 * angleViews[i].setText((angles[i]*360)/(2*Math.PI) + " "); } }
		 * mNumUpdates = (mNumUpdates + 1) % UI_UPDATE_FREQUENCY;
		 */
	}
}

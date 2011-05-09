package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.DeadReckoning;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class DeadReckoningActivity extends Activity {

	private static final int DEAD_RECKONING_TRAINING_CONSTANT = Menu.FIRST;
	private static final int DEAD_RECKONING_RESTART = Menu.FIRST + 1;
	private static final String TAG = "DeadReckoningActivity";
	
	private WebView mWebView;
	private DeadReckoning mDeadReckoning;
	private WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dead_reckoning_activity);
		
		mDeadReckoning = new DeadReckoning(this);
		
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
			default:
				throw new RuntimeException("Unexpected request code!");
		}
	}
	
	public void onSensorUpdate(float[] accel, float[] velocity,
			float[] displacement, float[] angularVelocity, float[] angles) {
		return;
	}
}

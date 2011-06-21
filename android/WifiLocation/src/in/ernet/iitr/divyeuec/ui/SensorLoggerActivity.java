package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.SensorLogger;
import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SensorLoggerActivity extends Activity {
	private Button mLoggingButton;
	private SensorLogger mSensorLogger;
	private WakeLock mWakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_logger_activity);
		
		mSensorLogger = new SensorLogger(this);
		
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SensorLoggerActivityWakeLock");
		
		mLoggingButton = (Button) findViewById(R.id.logging_button);
		mLoggingButton.setOnClickListener(new OnClickListener() {
			private int state = 0;
			final String labelText[] = new String[] { "Stop Logging Data", "Start Logging Data" };
			
			@Override
			public void onClick(View v) {
				mLoggingButton.setText(labelText[state]);
				
				switch(state) {
				case 0: // start logging
					mWakeLock.acquire();
					mSensorLogger.start();
					break;
				case 1:
					mSensorLogger.stop();
					mWakeLock.release();
					break;
				default:
					throw new RuntimeException("Invalid button state!");
				}
				
				state = (state + 1)%2;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorLogger.resume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorLogger.pause();
	}
	
}

package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.ui.views.MapView;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WifiSiteSurveyActivity extends Activity {
	protected static final int WIFI_RECORD_SAMPLES = 1;
	private MapView mMap;
	private TextView mXPos;
	private TextView mYPos;
	private Button mRecordButton;
	private EditText mSampleName;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_site_survey_activity);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mXPos = (TextView) findViewById(R.id.map_position_x);
		mYPos = (TextView) findViewById(R.id.map_position_y);
		mRecordButton = (Button) findViewById(R.id.btn_wifi_record_sample);
		mSampleName = (EditText) findViewById(R.id.wifi_sample_title);
		
		// The button should start off the WifiRecordSamplesActivity
		mRecordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent launchIntent = new Intent(WifiSiteSurveyActivity.this, WifiRecordSamplesActivity.class);
				String sampleName = mSampleName.getText().toString();
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_NAME, sampleName);
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_X, mMap.getmPosX());
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_Y, mMap.getmPosY());
				startActivityForResult(launchIntent, WIFI_RECORD_SAMPLES);
			}
		});
		
		
		// The Map should update the sample's location on every touch event.
		// So, use the touch listener to set the location on the map and update the labels.
		mMap = (MapView) findViewById(R.id.map);
		mMap.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mMap.setmPosX(event.getX());
				mMap.setmPosY(event.getY());
				mXPos.setText("" + mMap.getmPosX() + " (" + mMap.getmPosX()*mMap.getWidth() + ") ");
				mYPos.setText("" + mMap.getmPosY() + " (" + mMap.getmPosY()*mMap.getHeight() + ") ");
				return false;
			}
		});
		
	}

}

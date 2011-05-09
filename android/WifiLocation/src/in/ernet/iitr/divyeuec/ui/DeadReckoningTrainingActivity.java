package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.algorithms.DeadReckoning;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class DeadReckoningTrainingActivity extends Activity {

	public static final String KEY_THRESHOLD_VALUE = "ThresholdValue";
	public static final String KEY_TRAINING_VALUE = "TrainingValue";
	protected static final String TAG = "DeadReckoningTrainingActivity";
	private SeekBar mTrainingSlider;
	private TextView mTrainingValue;
	private SeekBar mThresholdSlider;
	private TextView mThresholdValue;
	private Intent mReturnValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dead_reckoning_training_const_slider);
		
		// This is the result of the activity
		mReturnValue = new Intent();
		
		mTrainingValue = (TextView) findViewById(R.id.training_value);
		mTrainingSlider = (SeekBar) findViewById(R.id.training_slider);
		mTrainingSlider.setOnSeekBarChangeListener(
			new SeekBar.OnSeekBarChangeListener() {
	
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					Log.i(TAG, "New Training Constant: " + progress);
					mTrainingValue.setText("" + (progress/1000.f));
					mReturnValue.putExtra(KEY_TRAINING_VALUE, progress/1000.f);
					setResult(RESULT_OK, mReturnValue);
				}
	
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					return;
				}
	
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					return;
				}
				
			}
		);
		
		mTrainingSlider.setProgress(Math.round(1000*getIntent().getFloatExtra(KEY_TRAINING_VALUE, DeadReckoning.DEFAULT_TRAINING_CONSTANT/1000.f)));
		
		mThresholdSlider = (SeekBar) findViewById(R.id.threshold_slider);
		mThresholdValue = (TextView) findViewById(R.id.threshold_value);
		mThresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				return;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				return;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Log.i(TAG, "New Threshold Value: " + (progress/1000.f));
				mThresholdValue.setText("" + progress/1000.f);
				mReturnValue.putExtra(KEY_THRESHOLD_VALUE, progress/1000.f);
				setResult(RESULT_OK, mReturnValue);
			}
		});
		mThresholdSlider.setProgress(Math.round(1000*getIntent().getFloatExtra(KEY_THRESHOLD_VALUE, DeadReckoning.DEFAULT_ACCEL_THRESHOLD/1000.f)));
	}
}

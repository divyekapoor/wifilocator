package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class DeadReckoning extends DefaultSensorCallbacks implements IAlgorithm {
	// Constants
	private static final int MAX_HISTORY_SIZE = 1000;

	private static final String TAG = "DeadReckoning";

	private static final int PEAK_HUNT = 0;
	private static final int VALLEY_HUNT = 1;

	// These constants are expected to be divided by 1000 before use
	public static int DEFAULT_TRAINING_CONSTANT = 1937;
	public static int DEFAULT_ACCEL_THRESHOLD = 1640;
	
	// Instance variables
	LinkedList<float[]> mAccelHistory;
	private float mStepDisplacement[];
	private float mTrainingConstant = DEFAULT_TRAINING_CONSTANT/1000.f;
	private float mAccelThreshold = DEFAULT_ACCEL_THRESHOLD/1000.f;
	
	private int mStepCount;
	private float mMinAccel;
	private float mMaxAccel;
	private int mState;
	
	// Reference to the Sensor Lifecycle Manager used to get the sensor data
	private SensorLifecycleManager mSensorLifecycleManager;

	private double mRoughAngle;

	
	// Raw path obtained by Dead Reckoning
	private ArrayList<float[]> mPath;
	
	public DeadReckoning(Context ctx) {
		init();		
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
	}

	private void init() {
		mAccelHistory = new LinkedList<float[]>();
		mAccelHistory.add(new float[3]);
		mAccelHistory.add(new float[3]);
		
		mStepDisplacement = new float[2];
		mStepCount = 0;
		mMaxAccel = 0.f;
		mMinAccel = 0.f;
		mRoughAngle = 0.f;
		mState = VALLEY_HUNT; // Used for detection of peaks and valleys from the accelerometer data.
	}
	
	@Override
	public void onAccelUpdate(float[] values, long deltaT, long timestamp) {
		// Remove low value sensor noise around 0
		// Also, threshold the sensor data so that only clean peaks are 
		// observed corresponding to steps
		if(Math.abs(values[2]) < mAccelThreshold) {
			values[2] = 0;
		}
		
		// Count local maxima
		synchronized(mAccelHistory) {
			float s0 = mAccelHistory.get(mAccelHistory.size()-2)[2], s1 = mAccelHistory.get(mAccelHistory.size()-1)[2], s2 = values[2];
			
			// Count peaks and valleys
			if((s2 - s1)*(s1 - s0) < 0) {
				if(s2 - s1 < 0) { // Peak Found
					if(mState == PEAK_HUNT) {
						// Count previous peak+valley pair and start off new counting.
						++mStepCount;
						double stepSize = mTrainingConstant*Math.pow(mMaxAccel - mMinAccel, 0.25);
						double radAngle = getAngleRadians();
						mStepDisplacement[0] += Math.sin(radAngle)*stepSize;
						mStepDisplacement[1] += Math.cos(radAngle)*stepSize;
						
						mMaxAccel = 0;
						mMinAccel = 0;
						
						mPath.add(new float[] { mStepDisplacement[0], mStepDisplacement[1], (float)radAngle});
					}
					mMaxAccel = Math.max(mMaxAccel, s1);
					if(s1 >= 0) {
						mState = VALLEY_HUNT;	
					}
					
				} else if (s2 - s1 > 0) { // Valley Found
					mMinAccel = Math.min(mMinAccel, s1);
					
					if(s1 <= 0) { // Found a valley in the negative region
						mState = PEAK_HUNT;
					}
				}
			}
			
			mAccelHistory.add(values);
			if(mAccelHistory.size() > MAX_HISTORY_SIZE) {
				mAccelHistory.removeFirst();
			}
		}
	}
	
	@Override
	public void onMagneticFieldUpdate(float[] values, long deltaT,
			long timestamp) {
		super.onMagneticFieldUpdate(values, deltaT, timestamp);
		mRoughAngle = Math.atan2(-values[0], values[1]);
	}

	public void setStepDisplacement(float[] stepCount) {
		this.mStepDisplacement = stepCount;
	}

	public String getStepDisplacement() {
		JSONArray jsonArray = new JSONArray();
		
		try {
			jsonArray.put(mStepDisplacement[0]);
			jsonArray.put(mStepDisplacement[1]);
		} catch (JSONException e) {
			Log.e(TAG, "JSON serialization error", e);
			throw new RuntimeException(e);
		}
		return jsonArray.toString();
	}
	
	public double getAngleRadians() {
		return mSensorLifecycleManager.getOrientation()[0];
		// return mRoughAngle;
	}
	
	public double getAngle() {
		return getAngleRadians()*360/(2*Math.PI);
		
	}
	
	public float getTrainingConstant() {
		return mTrainingConstant;
	}

	public void setTrainingConstant(float mTrainingConstant) {
		this.mTrainingConstant = mTrainingConstant;
	}
	
	public String getAccelHistory() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		synchronized(mAccelHistory) {
			for(float[] value : mAccelHistory) {
				jsonObject.accumulate("accel", value[2]);
			}
		}
		return jsonObject.toString();
	}
	
	public int getStepCount() {
		return mStepCount;
	}

	public void setStepCount(int mStepCount) {
		this.mStepCount = mStepCount;
	}

	public ArrayList<float[]> getmPath() {
		return mPath;
	}

	public void setmPath(ArrayList<float[]> mPath) {
		this.mPath = mPath;
	}

	@Override
	public void pause() {
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
	}

	@Override
	public void resume() {
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);

	}

	public void restart() {
		init();
	}

	public void setAccelThreshold(float mAccelThreshold) {
		this.mAccelThreshold = mAccelThreshold;
	}

	public float getAccelThreshold() {
		return mAccelThreshold;
	}

	
	
}

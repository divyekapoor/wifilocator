package in.ernet.iitr.divyeuec.sensors.internal;



import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

public class HWSensorEventListener implements SensorEventListener {

	// Constants for the serialize routines
	public static final String KEY_ANGLES_ARRAY = "KeyAnglesArray";
	public static final String KEY_ANGULAR_VELOCITY_ARRAY = "KeyAngularVelocityArray";
	public static final String KEY_ACCEL_ARRAY = "KeyAccelArray";
	public static final String KEY_VELOCITY_ARRAY = "KeyVelocityArray";
	public static final String KEY_DISPLACEMENT_ARRAY = "KeyDisplacementArray";
	public static final String KEY_LAST_GYRO_TIMESTAMP = "KeyLastGyroTimestamp";
	public static final String KEY_LAST_ACCEL_TIMESTAMP = "KeyLastAccelTimestamp";
	public static final String KEY_ACCEL_ACCURACY = "KeyAccelAccuracy";
	public static final String KEY_GYRO_ACCURACY = "KeyGyroAccuracy";
	public static final String KEY_LAST_GRAVITY_TIMESTAMP = "KeyLastGravityTimestamp";
	public static final String KEY_LAST_MAGNETIC_FIELD_TIMESTAMP = "KeyLastMagneticFieldTimestamp";
	public static final String KEY_GRAVITY_ARRAY = "KeyGravityArray";
	public static final String KEY_MAGNETIC_FIELD_ARRAY = "KeyMagneticFieldArray";

	public static final float NS_TO_S = 1.0f / 1e9f;

	// Logging tag
	private static final String TAG = "DeadReckoningSensorsListener";
	private static final String KEY_GRAVITY_ACCURACY = "KeyGravityAccuracy";
	private static final String KEY_MAGNETIC_FIELD_ACCURACY = "KeyMagneticFieldAccuracy";

	// Instance variables
	private int mGyroAccuracy;
	private int mAccelAccuracy;
	private int mGravityAccuracy;
	private int mMagneticFieldAccuracy;
	private long mLastAccelTimestamp;
	private long mLastGyroTimestamp;
	private long mLastGravityTimestamp;
	private long mLastMagneticFieldTimestamp;
	private float[] mDisplacement = new float[] { 0.f, 0.f, 0.f };
	private float[] mVelocity = new float[] { 0.f, 0.f, 0.f };
	private float[] mAccel = new float[] { 0.f, 0.f, 0.f };
	private float[] mAngularVelocity = new float[] { 0.f, 0.f, 0.f };
	private float[] mAngles = new float[] { 0.f, 0.f, 0.f };
	private float[] mGravity = new float[] { 0.f, 0.f, 0.f };
	private float[] mMagneticField = new float[] { 0.f, 0.f, 0.f };
	private float[] mTrueAccel = new float[] { 0.f, 0.f, 0.f };
	private float[] mI = new float[] { 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f,
			0.f };
	private float[] mR = new float[] { 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f,
			0.f };
	private float[] mPrevAccel = new float[] { 0.f,0.f,0.f};
	private float[] mPrevTrueAccel = new float[] {0.f, 0.f,0.f};
	private float[] mPrevDisplacement = new float[]{0.f,0.f,0.f};
	private float[] mPrevVelocity = new float[] {0.f,0.f,0.f};
	
	
	private ArrayList<IHWSensorEventCallback>  mCallbacks;

	private void init(Bundle pastValues) {
		mCallbacks = new ArrayList<IHWSensorEventCallback>();

		// Deserialize routine
		// Depends on initialization of the values to 0 before running of the
		// constructor
		mGyroAccuracy = pastValues.getInt(KEY_GYRO_ACCURACY, 0);
		mAccelAccuracy = pastValues.getInt(KEY_ACCEL_ACCURACY, 0);
		mGravityAccuracy = pastValues.getInt(KEY_GRAVITY_ACCURACY, 0);
		mMagneticFieldAccuracy = pastValues.getInt(KEY_MAGNETIC_FIELD_ACCURACY,
				0);
		mLastAccelTimestamp = pastValues.getLong(KEY_LAST_ACCEL_TIMESTAMP, 0);
		mLastGyroTimestamp = pastValues.getLong(KEY_LAST_GYRO_TIMESTAMP, 0);
		mLastGravityTimestamp = pastValues.getLong(KEY_LAST_GRAVITY_TIMESTAMP,
				0);
		mLastMagneticFieldTimestamp = pastValues.getLong(
				KEY_LAST_MAGNETIC_FIELD_TIMESTAMP, 0);
		mDisplacement = pastValues.containsKey(KEY_DISPLACEMENT_ARRAY) ? pastValues
				.getFloatArray(KEY_DISPLACEMENT_ARRAY) : mDisplacement;
		mVelocity = pastValues.containsKey(KEY_VELOCITY_ARRAY) ? pastValues
				.getFloatArray(KEY_VELOCITY_ARRAY) : mVelocity;
		mAccel = pastValues.containsKey(KEY_ACCEL_ARRAY) ? pastValues
				.getFloatArray(KEY_ACCEL_ARRAY) : mAccel;
		mAngularVelocity = pastValues.containsKey(KEY_ANGULAR_VELOCITY_ARRAY) ? pastValues
				.getFloatArray(KEY_ANGULAR_VELOCITY_ARRAY) : mAngularVelocity;
		mAngles = pastValues.containsKey(KEY_ANGLES_ARRAY) ? pastValues
				.getFloatArray(KEY_ANGLES_ARRAY) : mAngles;
		mGravity = pastValues.containsKey(KEY_GRAVITY_ARRAY) ? pastValues
				.getFloatArray(KEY_GRAVITY_ARRAY) : mGravity;
		mMagneticField = pastValues.containsKey(KEY_MAGNETIC_FIELD_ARRAY) ? pastValues
				.getFloatArray(KEY_MAGNETIC_FIELD_ARRAY) : mMagneticField;

	}

	public HWSensorEventListener() {	
		init(new Bundle());
	}

	public HWSensorEventListener(Bundle pastValues) {
		init(pastValues);
	}
	
	public boolean registerCallback(IHWSensorEventCallback callback) {
		mCallbacks.add(callback);
		return true;
	}
	
	public boolean unregisterCallback(IHWSensorEventCallback callback) {
		return mCallbacks.remove(callback);
	}
	
	public int callbackCount() {
		return mCallbacks.size();
	}

	public Bundle serialize() {
		Bundle b = new Bundle();
		b.putInt(KEY_GYRO_ACCURACY, mGyroAccuracy);
		b.putInt(KEY_ACCEL_ACCURACY, mAccelAccuracy);
		b.putInt(KEY_GRAVITY_ACCURACY, mGravityAccuracy);
		b.putInt(KEY_MAGNETIC_FIELD_ACCURACY, mMagneticFieldAccuracy);
		b.putLong(KEY_LAST_ACCEL_TIMESTAMP, mLastAccelTimestamp);
		b.putLong(KEY_LAST_GYRO_TIMESTAMP, mLastGyroTimestamp);
		b.putLong(KEY_LAST_GRAVITY_TIMESTAMP, mLastGravityTimestamp);
		b.putLong(KEY_LAST_MAGNETIC_FIELD_TIMESTAMP,
				mLastMagneticFieldTimestamp);
		b.putFloatArray(KEY_DISPLACEMENT_ARRAY, mDisplacement);
		b.putFloatArray(KEY_VELOCITY_ARRAY, mVelocity);
		b.putFloatArray(KEY_ACCEL_ARRAY, mAccel);
		b.putFloatArray(KEY_ANGULAR_VELOCITY_ARRAY, mAngularVelocity);
		b.putFloatArray(KEY_ANGLES_ARRAY, mAngles);
		b.putFloatArray(KEY_GRAVITY_ARRAY, mGravity);
		b.putFloatArray(KEY_MAGNETIC_FIELD_ARRAY, mMagneticField);
		return b;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Log.i(TAG, "Sensor: " + sensor.getName() + " Accuracy: " + accuracy);
		// Log.i(TAG, "Sensor: " + sensor.getName() + " MinDelay: " +
		// sensor.getMinDelay() + "us PowerUsage: " + sensor.getPower() +
		// "mA Resolution: " + sensor.getResolution() + "sensor-units Type:" +
		// sensor.getType() + " Vendor:" + sensor.getVendor() + " Version: " +
		// sensor.getVersion() + "ToString: " + sensor.toString());
		switch (sensor.getType()) {
		case Sensor.TYPE_LINEAR_ACCELERATION:
			mAccelAccuracy = accuracy;
			break;
		case Sensor.TYPE_GYROSCOPE:
			mGyroAccuracy = accuracy;
			break;
		case Sensor.TYPE_GRAVITY:
			mGravityAccuracy = accuracy;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mMagneticFieldAccuracy = accuracy;
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * The following equations are used to convert linear acceleration to
	 * displacements in 3 dimensions a(ti) = (v(ti) - v(ti-1))/(ti - ti-1) v(ti)
	 * = a(ti) * (deltaT) + v(ti-1) v(ti) = (d(ti) - d(ti-1))/(deltaT) d(ti) =
	 * v(ti) * deltaT + d(ti-1)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			long deltaT = -event.timestamp; // in nanoseconds
			switch (event.sensor.getType()) {
			case Sensor.TYPE_LINEAR_ACCELERATION:
				if (mLastAccelTimestamp == 0) {
					mLastAccelTimestamp = event.timestamp;
				}
				deltaT += mLastAccelTimestamp;
				mLastAccelTimestamp = event.timestamp;
				mPrevAccel = mAccel;
				mAccel = event.values.clone();
				updateTrueAccel();
				updateVelocityAndDisplacement(deltaT);
				
				for(IHWSensorEventCallback callback : mCallbacks) {
					callback.onAccelUpdate(mAccel, deltaT, mLastAccelTimestamp);
				}
				
				break;
			case Sensor.TYPE_GYROSCOPE:
				if (mLastGyroTimestamp == 0) {
					mLastGyroTimestamp = event.timestamp;
				}
				deltaT += mLastGyroTimestamp;
				mLastGyroTimestamp = event.timestamp;
				mAngularVelocity = event.values.clone();
				updateAngles(deltaT);
				
				for(IHWSensorEventCallback callback : mCallbacks) {
					callback.onGyroUpdate(mAngularVelocity, deltaT, mLastGyroTimestamp);
				}
				
				break;

			case Sensor.TYPE_GRAVITY:
				if (mLastGravityTimestamp == 0) {
					mLastGravityTimestamp = event.timestamp;
				}
				deltaT += mLastGravityTimestamp;
				mGravity = event.values.clone();
				updateRotationMatrices();
				
				for(IHWSensorEventCallback callback : mCallbacks) {
					callback.onGravityUpdate(mGravity, deltaT, mLastGravityTimestamp);
				}
				break;

			case Sensor.TYPE_MAGNETIC_FIELD:
				if (mLastMagneticFieldTimestamp == 0) {
					mLastMagneticFieldTimestamp = event.timestamp;
				}
				deltaT += mLastMagneticFieldTimestamp;
				mMagneticField = event.values.clone();
				updateRotationMatrices();
				
				for(IHWSensorEventCallback callback : mCallbacks) {
					callback.onMagneticFieldUpdate(mMagneticField, deltaT, mLastMagneticFieldTimestamp);
				}
				break;
			}
		}
	}

	private void updateRotationMatrices() {
		SensorManager.getRotationMatrix(mR, mI, mGravity, mMagneticField);
		updateTrueAccel();
	}

	private void updateTrueAccel() {
		mPrevTrueAccel = mTrueAccel.clone();
		mTrueAccel[0] = mR[0] * mAccel[0] + mR[1] * mAccel[1] + mR[2]
				* mAccel[2];
		mTrueAccel[1] = mR[3] * mAccel[0] + mR[4] * mAccel[1] + mR[5]
				* mAccel[2];
		mTrueAccel[2] = mR[6] * mAccel[0] + mR[7] * mAccel[1] + mR[8]
				* mAccel[2];
	}

	private void updateAngles(long deltaT) {
		for (int i = 0; i < 3; ++i) {
			// time is in nanoseconds. Normalize to seconds
			mAngles[i] += (mAngularVelocity[i] * deltaT) * NS_TO_S;
		}
	}

	private void updateVelocityAndDisplacement(long deltaT) {
		/*for (int i = 0; i < 3; ++i) {
			mVelocity[i] += (mTrueAccel[i] * deltaT) * NS_TO_S;
			mDisplacement[i] += (mVelocity[i] * deltaT) * NS_TO_S; // Double
																	// Integration
		}*/
		// Use verlet integration instead of eulerian integration
		// Formulae:
		//    x(tn+1) = 2 x(tn) - x(tn-1) + a(tn) dT^2
		//    v(tn+1) = v(tn) + (a(tn) + a(tn+1))/2 * dT
		float[] prevDisplacement = mDisplacement.clone();
		float[] prevVelocity = mVelocity.clone();
		
		for(int i = 0; i < 3; ++i) {
			mDisplacement[i] = 2*mDisplacement[i] - mPrevDisplacement[i] + (mPrevAccel[i]*deltaT*deltaT)*NS_TO_S*NS_TO_S;
			mVelocity[i] += mPrevVelocity[i] + (((mPrevAccel[i] + mAccel[i])/2.0f)* deltaT) * NS_TO_S;
		}
		mPrevDisplacement = prevDisplacement;
		mPrevVelocity = prevVelocity;
	}

	/*
	 * This private function converts an input array to its equivalent JSON
	 * string representation.
	 * 
	 * The arrays cannot be directly exposed to JavaScript as they are
	 * references. Hence, the arrays need to be converted into a JSON string
	 * which will be exposed to JavaScript for conversion back into an array.
	 * Ugh!
	 */
	private JSONArray toJSON(float[] array) {
		JSONArray jsonArray = new JSONArray();
		for (float value : array) {
			if (Float.isInfinite(value) || Float.isNaN(value)) {
				value = 0;
			}
			
			try {
				jsonArray.put((double) value);
			} catch (JSONException e) {
				Log.e(TAG, "JSON Array conversion raised exception.", e);
			}
		}
		return jsonArray;
	}

	/*
	 * Getters for all the member variables follow.
	 */

	public int getmGyroAccuracy() {
		return mGyroAccuracy;
	}

	public int getmAccelAccuracy() {
		return mAccelAccuracy;
	}

	public long getmLastAccelTimestamp() {
		return mLastAccelTimestamp;
	}

	public long getmLastGyroTimestamp() {
		return mLastGyroTimestamp;
	}

	public String getmDisplacement() {
		return toJSON(mDisplacement).toString();
	}

	public String getmVelocity() {
		return toJSON(mVelocity).toString();
	}

	public String getmAccel() {
		return toJSON(mAccel).toString();
	}

	public String getmAngularVelocity() {
		return toJSON(mAngularVelocity).toString();
	}

	public String getmAngles() {
		return toJSON(mAngles).toString();
	}

	/*
	 * Extra getter function that returns a single JSON object to the JavaScript
	 * code. Hopefully, it will be much faster as the Object -> JSON -> Native
	 * Javascript Object conversion needs to be done only once.
	 */
	public String getAllData() {
		synchronized (this) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(KEY_ACCEL_ACCURACY, mAccelAccuracy);
				jsonObject.put(KEY_GYRO_ACCURACY, mGyroAccuracy);
				jsonObject.put(KEY_LAST_ACCEL_TIMESTAMP, mLastAccelTimestamp);
				jsonObject.put(KEY_LAST_GYRO_TIMESTAMP, mLastGyroTimestamp);
				jsonObject.put(KEY_ACCEL_ARRAY, toJSON(mAccel));
				jsonObject.put(KEY_ANGLES_ARRAY, toJSON(mAngles));
				jsonObject.put(KEY_ANGULAR_VELOCITY_ARRAY,
						toJSON(mAngularVelocity));
				jsonObject.put(KEY_DISPLACEMENT_ARRAY, toJSON(mDisplacement));
				jsonObject.put(KEY_VELOCITY_ARRAY, toJSON(mVelocity));

				// Log.i(TAG, jsonObject.getString(KEY_ACCEL_ARRAY));
				String jsonResult = jsonObject.toString().replace("*", "")
						.replace(")", ""); // HACK: The two replace calls are
											// hacks as exposing the strings
											// wasn't working well and producing
											// invalid JSON. It shouldn't
											// be strictly required.
				// Log.i(TAG, jsonResult);
				return jsonResult;
			} catch (JSONException e) {
				Log.e(TAG, "JSON conversion failed.", e);
				return "";
			}
		}
	}

	/*
	 * The static variables need to be exposed as member functions so as to be
	 * accessible via javascript method calls.
	 * 
	 * A rather ugly method but we need to work with this as it is a limitation
	 * of the Android platform.
	 * 
	 * STATIC KEY VALUE GETTERS:-
	 */

	public String getKeyAnglesArray() {
		return KEY_ANGLES_ARRAY;
	}

	public String getKeyAngularVelocityArray() {
		return KEY_ANGULAR_VELOCITY_ARRAY;
	}

	public String getKeyAccelArray() {
		return KEY_ACCEL_ARRAY;
	}

	public String getKeyVelocityArray() {
		return KEY_VELOCITY_ARRAY;
	}

	public String getKeyDisplacementArray() {
		return KEY_DISPLACEMENT_ARRAY;
	}

	public String getKeyLastGyroTimestamp() {
		return KEY_LAST_GYRO_TIMESTAMP;
	}

	public String getKeyLastAccelTimestamp() {
		return KEY_LAST_ACCEL_TIMESTAMP;
	}

	public String getKeyAccelAccuracy() {
		return KEY_ACCEL_ACCURACY;
	}

	public String getKeyGyroAccuracy() {
		return KEY_GYRO_ACCURACY;
	}
	
	

}

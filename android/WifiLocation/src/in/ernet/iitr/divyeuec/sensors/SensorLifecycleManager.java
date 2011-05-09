package in.ernet.iitr.divyeuec.sensors;

import in.ernet.iitr.divyeuec.sensors.internal.HWSensorEventListener;
import in.ernet.iitr.divyeuec.sensors.internal.WifiScanEventListener;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class SensorLifecycleManager {

	private static final String TAG = "SensorLifecycleManager";

	private Context mCtx;
	private SensorManager mSensorManager;
	private HWSensorEventListener mSensorEventListener;
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
	private WifiScanEventListener mWifiScanResultReceiver;

	private boolean mDisableWifiOnPause = false;

	public static final int SENSOR_ACCELEROMETER = 1;
	public static final int SENSOR_GRAVITY = 2;
	public static final int SENSOR_MAGNETISM = 3;
	public static final int SENSOR_GYROSCOPE = 4;
	public static final int SENSOR_WIFI = 100;

	// This is a Singleton class
	private static SensorLifecycleManager mInstance = null;

	public static SensorLifecycleManager getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new SensorLifecycleManager(ctx);
		}

		return mInstance;
	}

	private SensorLifecycleManager(Context ctx) {
		mCtx = ctx.getApplicationContext();
		mSensorManager = (SensorManager) mCtx
				.getSystemService(Context.SENSOR_SERVICE);
		mWifiManager = (WifiManager) mCtx
				.getSystemService(Context.WIFI_SERVICE);
		mSensorEventListener = new HWSensorEventListener();
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, TAG);
		mWifiScanResultReceiver = new WifiScanEventListener(mCtx);
	}

	private void resumeAll() {
		resumeHWEventListeners();
		resumeWifiEventListeners();
	}

	private void resumeWifiEventListeners() {
		mWifiLock.acquire();

		IntentFilter scanIntent = new IntentFilter();
		scanIntent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mCtx.registerReceiver(mWifiScanResultReceiver.getBroadcastReceiverInstance(), scanIntent);

		if (!mWifiManager.isWifiEnabled() && mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLING) {
			mDisableWifiOnPause = true;
			mWifiManager.setWifiEnabled(true);
		}

		mWifiManager.startScan();
	}

	private void resumeHWEventListeners() {
		int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
		mSensorManager.registerListener(mSensorEventListener, mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
				SENSOR_DELAY);
		mSensorManager.registerListener(mSensorEventListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SENSOR_DELAY);
		mSensorManager.registerListener(mSensorEventListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SENSOR_DELAY);
		mSensorManager.registerListener(mSensorEventListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SENSOR_DELAY);
	}

	private void pauseAll() {
		pauseHWEventListeners();
		pauseWifiEventListeners();
	}

	private void pauseWifiEventListeners() {
		mCtx.unregisterReceiver(mWifiScanResultReceiver.getmWifiEventReceiver());
		mWifiScanResultReceiver.setmWifiEventReceiver(null);
		
		mWifiLock.release();
		if(mDisableWifiOnPause) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	private void pauseHWEventListeners() {
		mSensorManager.unregisterListener(mSensorEventListener);
	}

	public void registerCallback(ISensorCallback callback, int sensorEventType) {
		boolean resumeRequired = false;

		switch (sensorEventType) {
		case SENSOR_ACCELEROMETER:
		case SENSOR_GYROSCOPE:
		case SENSOR_GRAVITY:
		case SENSOR_MAGNETISM:
			if (mSensorEventListener.callbackCount() == 0) {
				resumeRequired = true;
			}
			
			mSensorEventListener.registerCallback(callback);

			if (resumeRequired && mSensorEventListener.callbackCount() > 0) {
				resumeHWEventListeners();
			}

			break;

		case SENSOR_WIFI:
			if (mWifiScanResultReceiver.callbackCount() == 0) {
				resumeRequired = true;
			}

			mWifiScanResultReceiver.registerCallback(callback);

			if (resumeRequired && mWifiScanResultReceiver.callbackCount() > 0) {
				resumeWifiEventListeners();
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid eventType " + sensorEventType
					+ " for SensorEvents");
		}
	}

	public void unregisterCallback(ISensorCallback callback, int sensorEventType) {
		switch (sensorEventType) {
		case SENSOR_ACCELEROMETER:
		case SENSOR_GYROSCOPE:
		case SENSOR_GRAVITY:
		case SENSOR_MAGNETISM:
			mSensorEventListener.unregisterCallback(callback);
			if (mSensorEventListener.callbackCount() == 0) {
				pauseHWEventListeners();
			}
			break;

		case SENSOR_WIFI:
			mWifiScanResultReceiver.unregisterCallback(callback);
			if (mWifiScanResultReceiver.callbackCount() == 0) {
				pauseWifiEventListeners();
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid eventType "
					+ sensorEventType + " for SensorEvents");
		}
	}

	// Unregister from all possible callbacks.
	// Useful in the cases when you don't know what kind of callbacks
	// the object has registered itself.
	public void unregisterCallback(ISensorCallback callback) {
		unregisterCallback(callback, SENSOR_ACCELEROMETER);
		unregisterCallback(callback, SENSOR_GRAVITY);
		unregisterCallback(callback, SENSOR_GYROSCOPE);
		unregisterCallback(callback, SENSOR_MAGNETISM);
		unregisterCallback(callback, SENSOR_WIFI);
	}
	
	public float[] getRotationMatrix() {
		return mSensorEventListener.getRotationMatrix();
	}
	
	public float[] getInclinationMatrix() {
		return mSensorEventListener.getInclinationMatrix();
	}
	
	public float[] getOrientation() {
		float[] values = new float[3];
		return SensorManager.getOrientation(getRotationMatrix(), values);
	}
}

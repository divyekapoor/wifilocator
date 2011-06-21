package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.sensors.ISensorCallback;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Environment;
import android.util.Log;

public class SensorLogger implements IAlgorithm, ISensorCallback {

	private static final String SAMPLES_DIR = Environment.getExternalStorageDirectory() + File.separator + "samples";
	private static final int STOPPED = 0;
	private static final int STARTED = 1;
	private static final int PAUSED = 2;
	private static final String TAG = "SensorLogger";
	
	private SensorLifecycleManager mSensorLifecycleManager;
	private int mState;
	private FileWriter mAccelFileWriter;
	private FileWriter mGyroFileWriter;
	private FileWriter mMagFileWriter;
	private FileWriter mAngleFileWriter;
	private FileWriter mWifiFileWriter;
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
	
	public SensorLogger(Context ctx) {
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
		mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, TAG);
		mState = STOPPED;
	}
	
	public void start() {
		
		mAccelFileWriter = getFile("accel", "csv");
		mGyroFileWriter = getFile("gyro", "csv");
		mMagFileWriter = getFile("mag", "csv");
		mAngleFileWriter = getFile("angle", "csv");
		mWifiFileWriter = getFile("wifi", "csv");
		
		mWifiLock.acquire();
		
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_GYROSCOPE);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_WIFI);
		
		mWifiManager.startScan();
		
		mState = STARTED;
		
	}

	public void stop() {
		mWifiLock.release();
		
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_GYROSCOPE);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_WIFI);
		
		try {
			mAccelFileWriter.flush();
			mAccelFileWriter.close();
			
			mGyroFileWriter.flush();
			mGyroFileWriter.close();
			
			mMagFileWriter.flush();
			mMagFileWriter.close();
			
			mAngleFileWriter.flush();
			mAngleFileWriter.close();
			
			mWifiFileWriter.flush();
			mWifiFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Flushing and closing files failed!", e);
			throw new RuntimeException(e);
		}
		
		
		mState = STOPPED;
	}

	@Override
	public void resume() {
		// Started state -> Invalid!!!
		// Stopped state -> No Op
		// Paused state -> restart
		if(mState == PAUSED) { 
			start();
		}
	}

	@Override
	public void pause() {
		// Started state -> Paused
		// Stopped state -> No op
		// Paused state -> Invalid!!!
		if(mState == STARTED) {
			stop();
			mState = PAUSED;
		}
	}

	@Override
	public void onAccelUpdate(float[] values, long deltaT, long timestamp) {
		persistToFile(mAccelFileWriter, values, deltaT, timestamp);
	}

	@Override
	public void onGravityUpdate(float[] values, long deltaT, long timestamp) {
		persistToFile(mAngleFileWriter, mSensorLifecycleManager.getOrientation(), deltaT, timestamp);
	}

	@Override
	public void onMagneticFieldUpdate(float[] values, long deltaT,
			long timestamp) {
		persistToFile(mMagFileWriter, values, deltaT, timestamp);
		persistToFile(mAngleFileWriter, mSensorLifecycleManager.getOrientation(), deltaT, timestamp);
	}

	@Override
	public void onGyroUpdate(float[] values, long deltaT, long timestamp) {
		persistToFile(mGyroFileWriter, values, deltaT, timestamp);
	}

	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		Date now = new Date(System.currentTimeMillis());
		LocationFingerprint locationFingerprint = new LocationFingerprint(LocationFingerprint.RAVINDRA_BHAWAN_MAP_ID, now, mSensorLifecycleManager.getOrientation()[0], 0.f, 0.f, scanResults, "wifi_log");
		try {
			mWifiFileWriter.write(now.getTime() + "," + locationFingerprint.getmFingerprintData().toString() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't write to Wifi Log file!", e);
			throw new RuntimeException(e);
		}
		
		mWifiManager.startScan(); // For immediate retry for maximum resolution of Wifi data
	}

	private FileWriter getFile(String dataType, String extension) {
		Date d = new Date(System.currentTimeMillis());
		String dtime = d.getDate() + "-" + d.getMonth()
		+ "-" + (d.getYear() + 1900) + "-" + d.getHours() + "-"
		+ d.getMinutes() + "-" + d.getSeconds();
		
		FileWriter f = null;
		try {
			f = new FileWriter(new File(SAMPLES_DIR, dataType + "." + dtime + "." + extension));
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "File couldn't be opened for writing!");
			throw new RuntimeException(e);
		}
		return f;
	}

	private void persistToFile(FileWriter fileWriter, float[] values, long deltaT, long timestamp) {
		try {
			fileWriter.write(timestamp + "," + deltaT + "," + values[0] + "," + values[1] + "," + values[2] + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Writing sensor data to file failed!", e);
			throw new RuntimeException(e);
		}
	}

}

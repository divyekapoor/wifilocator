package in.ernet.iitr.divyeuec.algorithms.wifi;

import in.ernet.iitr.divyeuec.algorithms.IAlgorithm;
import in.ernet.iitr.divyeuec.sensors.ISensorCallback;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class OrientedWifiListing extends WifiListing implements IAlgorithm, ISensorCallback {
	
	private static final String KEY_ANGLE = "KeyAngle";
	private ArrayList<IAngleUpdateCallback> mAngleUpdateCallbacks;

	public OrientedWifiListing(Context ctx) {
		super(ctx);
		mAngleUpdateCallbacks = new ArrayList<IAngleUpdateCallback>();
	}
	
	
	public float getAngleRadians() {
		return mSensorLifecycleManager.getOrientation()[0];
	}
	
	public float getAngle() {
		return (float) (getAngleRadians()*360/(2*Math.PI));
	}
	
	@Override
	public void onGravityUpdate(float[] values, long deltaT, long timestamp) {
		super.onGravityUpdate(values, deltaT, timestamp);
		notifyAngleUpdates();
	}
	
	@Override
	public void onMagneticFieldUpdate(float[] values, long deltaT,
			long timestamp) {
		super.onMagneticFieldUpdate(values, deltaT, timestamp);
		notifyAngleUpdates();
	}


	private void notifyAngleUpdates() {
		for(IAngleUpdateCallback cb: mAngleUpdateCallbacks) {
			cb.onAngleUpdate();
		}
	}
	
	public boolean registerAngleUpdateCallback(IAngleUpdateCallback callback) {
		return mAngleUpdateCallbacks.add(callback);
	}
	
	public boolean unregisterAngleUpdateCallbacks(IAngleUpdateCallback callback) {
		return mAngleUpdateCallbacks.remove(callback);
	}
	
	public int angleUpdateCallbackCount() {
		return mAngleUpdateCallbacks.size();
	}
	
	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		// Augment the scan results with the angle of scan
		float angle = getAngle();
		for(Iterator<Map<String,String>> it = scanResults.iterator(); it.hasNext();) {
			Map<String, String> scanResult = it.next();
			scanResult.put(KEY_ANGLE, "" + angle);
		}
		super.onWifiScanResultsAvailable(scanResults);
	}
	
	@Override
	public void resume() {
		super.resume();
		// You need to register for these 2 events to get orientation information updated
		// regularly by the system.
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
	}

	@Override
	public void pause() {
		// You need to register for these 2 events to get orientation information updated
		// regularly by the system.
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		super.pause();
	}

}

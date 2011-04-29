package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.util.LinkedList;

import android.content.Context;

public class DeadReckoning extends DefaultSensorCallbacks implements IAlgorithm {
	// Constants
	private static final int MAX_HISTORY_SIZE = 1000;
	
	// Instance variables
	LinkedList<float[]> mAccelHistory = new LinkedList<float[]>();
	private int mStepCount = 0;

	private SensorLifecycleManager mSensorLifecycleManager;
	
	public DeadReckoning(Context ctx) {
		mAccelHistory.add(new float[3]);
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
	}
	
	@Override
	public void onAccelUpdate(float[] values, long deltaT, long timestamp) {
		// Z axis zero crossing counts.
		if(values[2]*mAccelHistory.getLast()[2] < 0) {
			setStepCount(getStepCount() + 1);
		}
		
		mAccelHistory.add(values);
		if(mAccelHistory.size() > MAX_HISTORY_SIZE) {
			mAccelHistory.removeFirst();
		}
	}

	public void setStepCount(int mStepCount) {
		this.mStepCount = mStepCount;
	}

	public int getStepCount() {
		return mStepCount;
	}

	@Override
	public void pause() {
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
	}

	@Override
	public void resume() {
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
	}
	
}

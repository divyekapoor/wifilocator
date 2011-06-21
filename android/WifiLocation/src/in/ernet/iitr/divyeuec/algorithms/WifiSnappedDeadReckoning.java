package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.algorithms.wifi.distance.ILocationFingerprintDistanceAlgorithm;
import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.ISensorCallback;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.db.PersistenceFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiSnappedDeadReckoning extends DefaultSensorCallbacks implements
		IAlgorithm, ISensorCallback, IReckoningMethod {

	private static final String TAG = "WifiSnappedDeadReckoning";
	private DeadReckoning mDeadReckoning;
	private SensorLifecycleManager mSensorLifecycleManager;
	private WifiManager mWifiManager;
	private ILocationFingerprintDistanceAlgorithm mDistanceAlgorithm;
	private Context mCtx;

	public WifiSnappedDeadReckoning(Context ctx, ILocationFingerprintDistanceAlgorithm algorithm) {
		mCtx = ctx;
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
		mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		mDeadReckoning = new DeadReckoning(ctx);
		mDistanceAlgorithm = algorithm;
	}
	
	@Override
	public void pause() {
		mDeadReckoning.pause();
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_WIFI);
	}
	
	@Override
	public void resume() {
		mDeadReckoning.resume();
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_WIFI);
		
	}
	
	
	public void restart() {
		mDeadReckoning.restart();
	}
	
	

	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		Log.i(TAG, "Wifi Scan results available.");
		LocationFingerprint currentLocationFingerprint = new LocationFingerprint(LocationFingerprint.RAVINDRA_BHAWAN_MAP_ID, new Date(System.currentTimeMillis()), (float) mDeadReckoning.getAngle(), 0, 0, scanResults, "unknown");
		List<LocationFingerprint> allSamples = PersistenceFactory.getInstance(mCtx).getAllSamples();
		LocationFingerprint closestSample = null;
		double minDistance = Double.POSITIVE_INFINITY;
		for(LocationFingerprint f : allSamples) {
			double distance = mDistanceAlgorithm.distance(f, currentLocationFingerprint);
			if(distance < minDistance) {
				minDistance = distance;
				closestSample = f;
			}
		}
		
		if(closestSample != null) {
			Log.i(TAG, "Closest distance point identified.");
			ArrayList<float[]> path = mDeadReckoning.getmPath();
			float[] displacement = new float[] { closestSample.getmX(), closestSample.getmY() };
			
			synchronized(path) {
				path.add(displacement);
				mDeadReckoning.setmPath(path);
			}
			mDeadReckoning.setLocation(displacement);
		}
		
		
		mWifiManager.startScan(); // Needed - else scans won't be repeated as fast as possible
	}

	//------------------------------------
	// Adapter pattern for DeadReckoning
	
	
	public double getStepSize() {
		return mDeadReckoning.getStepSize();
	}

	
	public void setLocation(float[] stepDisplacement) {
		mDeadReckoning.setLocation(stepDisplacement);
	}
	
	public void setLocation(double x, double y) {
		this.setLocation(new float[] { (float)x, (float)y });
	}

	
	public String getLocationJSON() {
		return mDeadReckoning.getLocationJSON();
	}

	
	public double getAngleRadians() {
		return mDeadReckoning.getAngleRadians();
	}
	
	public double getAngle() {
		return mDeadReckoning.getAngle();
	}

	
	public float getTrainingConstant() {
		return mDeadReckoning.getTrainingConstant();
	}

	
	public void setTrainingConstant(float mTrainingConstant) {
		mDeadReckoning.setTrainingConstant(mTrainingConstant);
	}

	
	public String getAccelHistory() throws JSONException {
		return mDeadReckoning.getAccelHistory();
	}

	
	public int getStepCount() {
		return mDeadReckoning.getStepCount();
	}

	
	public void setStepCount(int mStepCount) {
		mDeadReckoning.setStepCount(mStepCount);
	}

	
	public ArrayList<float[]> getmPath() {
		return mDeadReckoning.getmPath();
	}

	
	public void setmPath(ArrayList<float[]> mPath) {
		mDeadReckoning.setmPath(mPath);
	}

	
	public void setAccelThreshold(float mAccelThreshold) {
		mDeadReckoning.setAccelThreshold(mAccelThreshold);
	}

	
	public float getAccelThreshold() {
		return mDeadReckoning.getAccelThreshold();
	}

	
	public void setmStartX(float mStartX) {
		mDeadReckoning.setmStartX(mStartX);
	}

	
	public float getmStartX() {
		return mDeadReckoning.getmStartX();
	}

	
	public void setmStartY(float mStartY) {
		mDeadReckoning.setmStartY(mStartY);
	}

	
	public float getmStartY() {
		return mDeadReckoning.getmStartY();
	}

	
	public float[] getStartPos() {
		return mDeadReckoning.getStartPos();
	}

	
	public void setStartPos(float x, float y) {
		mDeadReckoning.setStartPos(x, y);
	}

	@Override
	public int getmMapWidth() {
		return mDeadReckoning.getmMapWidth();
	}

	@Override
	public void setmMapWidth(int mMapWidth) {
		mDeadReckoning.setmMapWidth(mMapWidth);
	}

	@Override
	public int getmMapHeight() {
		return mDeadReckoning.getmMapHeight();
	}

	@Override
	public void setmMapHeight(int mMapHeight) {
		mDeadReckoning.setmMapHeight(mMapHeight);
	}
	
	@Override
	public String getmPathJSON() {
		return mDeadReckoning.getmPathJSON();
	}
	
	public float[] getLocation() {
		return mDeadReckoning.getLocation();
	}
	
	@Override
	public boolean isLogging() {
		return mDeadReckoning.isLogging();
	}

	@Override
	public void stopLogging() {
		mDeadReckoning.stopLogging();
	}

	@Override
	public void startLogging() {
		mDeadReckoning.startLogging();
	}
	

}

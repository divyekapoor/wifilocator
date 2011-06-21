package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.sensors.DefaultSensorCallbacks;
import in.ernet.iitr.divyeuec.sensors.SensorLifecycleManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class DeadReckoning extends DefaultSensorCallbacks implements IAlgorithm, IReckoningMethod {
	private static final String SAMPLES_DIR = Environment.getExternalStorageDirectory() + File.separator + "samples";
	private static final int DEFAULT_MAP_HEIGHT = 480;
	private static final int DEFAULT_MAP_WIDTH = 640;

	// Constants
	private static final int MAX_HISTORY_SIZE = 10;

	private static final String TAG = "DeadReckoning";

	private static final int PEAK_HUNT = 0;
	private static final int VALLEY_HUNT = 1;

	// These constants are expected to be divided by 1000 before use
	public static int DEFAULT_TRAINING_CONSTANT = 4964; // 5200; // 3300; // 1937;
	public static int DEFAULT_ACCEL_THRESHOLD = 1300; // 1840;
	
	// Instance variables
	LinkedList<float[]> mAccelHistory;
	private float mLocation[];
	private float mTrainingConstant = DEFAULT_TRAINING_CONSTANT/1000.f;
	private float mAccelThreshold = DEFAULT_ACCEL_THRESHOLD/1000.f;
	private float mStartX; // on a 0-1 scale based on the map
	private float mStartY;
	private int mMapWidth = DEFAULT_MAP_WIDTH;
	private int mMapHeight = DEFAULT_MAP_HEIGHT;
	
	// Raw path obtained by Dead Reckoning
	private ArrayList<float[]> mPath;

	
	private int mStepCount;
	private float mMinAccel;
	private float mMaxAccel;
	private int mState;
	
	// Reference to the Sensor Lifecycle Manager used to get the sensor data
	private SensorLifecycleManager mSensorLifecycleManager;

	private double mRoughAngle;
	private boolean mIsLogging;
	private FileWriter mAccelLogFileWriter;
	private FileWriter mStepLogFileWriter;

	public DeadReckoning(Context ctx) {
		init();		
		mSensorLifecycleManager = SensorLifecycleManager.getInstance(ctx);
	}

	protected void init() {
		mAccelHistory = new LinkedList<float[]>();
		mAccelHistory.add(new float[3]); // Added to avoid bounds checks while accessing
		mAccelHistory.add(new float[3]); // indices [i-1] and [i-2]
		
		mPath = new ArrayList<float[]>();
		
		mLocation = new float[2];
		mStepCount = 0;
		mMaxAccel = 0.f;
		mMinAccel = 0.f;
		mRoughAngle = 0.f;
		mStartX = 0.f;
		mStartY = 0.f;
		mState = VALLEY_HUNT; // Used for detection of peaks and valleys from the accelerometer data.
		
		mIsLogging = false;
	}
	
	@Override
	public void onAccelUpdate(float[] values, long deltaT, long timestamp) {
		// Remove low value sensor noise around 0
		// Also, threshold the sensor data so that only clean peaks are 
		// observed corresponding to steps
		if(this.isLogging()) {
			try {
				mAccelLogFileWriter.write("" + timestamp + "," + deltaT + "," + values[2] + "\n");
			} catch (IOException e) {
				Log.e(TAG, "Log file write for acceleration failed!!!\n", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		if(Math.abs(values[2]) < mAccelThreshold) {
			values[0] = values[1] = values[2] = 0;
		}

		// Count local maxima
		synchronized(mAccelHistory) {
			float s0 = mAccelHistory.get(mAccelHistory.size()-2)[2], s1 = mAccelHistory.get(mAccelHistory.size()-1)[2], s2 = values[2];
			
			// Count peaks and valleys
			if((s2 - s1)*(s1 - s0) < 0) {
				if(s2 - s1 < 0 && s1 > 0) { // Peak Found
					if(mState == PEAK_HUNT) {
						// Count previous peak+valley pair and start off new counting. 
						++mStepCount;

						double stepSize = getStepSize();
						double radAngle = getAngleRadians();
						
						if(this.isLogging()) {
							try {
								mStepLogFileWriter.write("" + timestamp + "," + deltaT + "," + stepSize + "," + Math.toDegrees(radAngle) + "\n");
							} catch (IOException e) {
								Log.e(TAG, "Writing to step log file failed!", e);
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}

						
						// TODO Remove this offset!
						double offset = Math.toRadians(10); // 10 degree offset because of map
						radAngle -= offset;
						if(radAngle < -Math.PI)
							radAngle += 2*Math.PI;
						
						// Expected to set the new location of the person
						updateLocation(stepSize, radAngle);
						
						synchronized(mPath) {
							mPath.add(new float[] { mLocation[0], mLocation[1], (float)radAngle});
						}

						mMaxAccel = 0;
						mMinAccel = 0;
						
						mState = VALLEY_HUNT;
					}
					mMaxAccel = Math.max(mMaxAccel, s1);	
				} else if (s2 - s1 > 0 && s1 < 0) { // Valley Found
					mMinAccel = Math.min(mMinAccel, s1);
					if(mState == VALLEY_HUNT) {
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

	/* TODO Dead code. Remove if not needed 2 versions later.
	 * 
	 *  private double getAccelMagnitude(float[] rawAccel) {
		double netAccel = Math.sqrt(rawAccel[0]*rawAccel[0] + rawAccel[1]*rawAccel[1] + rawAccel[2]*rawAccel[2]);
		return netAccel;
	} */

	protected void updateLocation(double stepSize, double radAngle) {
		mLocation[0] += Math.sin(radAngle)*stepSize/mMapWidth;
		mLocation[1] += -Math.cos(radAngle)*stepSize/mMapHeight; // negative sign due to image coordinate system v/s True North angle
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getStepSize()
	 */
	@Override
	public double getStepSize() {
		double stepSize = mTrainingConstant*Math.pow(mMaxAccel - mMinAccel, 0.25);
		return stepSize;
	}
	
	@Override
	public void onMagneticFieldUpdate(float[] values, long deltaT,
			long timestamp) {
		super.onMagneticFieldUpdate(values, deltaT, timestamp);
		mRoughAngle = Math.atan2(-values[0], values[1]);
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setStepDisplacement(float[])
	 */
	@Override
	public void setLocation(float[] location) {
		this.mLocation = location;
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setStepDisplacement(float[])
	 */
	@Override
	public void setLocation(double x, double y) { // 0-1 coordinate system
		setLocation(new float[] { (float)x, (float)y });
	}

	@Override
	public float[] getLocation() {
		return mLocation;
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getStepDisplacement()
	 */
	@Override
	public String getLocationJSON() { // Coordinate system: 0-1 on map
		JSONArray jsonArray = new JSONArray();
		
		try {
			jsonArray.put(mLocation[0]);
			jsonArray.put(mLocation[1]);
		} catch (JSONException e) {
			Log.e(TAG, "JSON serialization error", e);
			throw new RuntimeException(e);
		}
		return jsonArray.toString();
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getAngleRadians()
	 */
	@Override
	public double getAngleRadians() {
		return mSensorLifecycleManager.getOrientation()[0];
		// return mRoughAngle;
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getAngle()
	 */
	@Override
	public double getAngle() {
		return getAngleRadians()*360/(2*Math.PI);
		
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getTrainingConstant()
	 */
	@Override
	public float getTrainingConstant() {
		return mTrainingConstant;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setTrainingConstant(float)
	 */
	@Override
	public void setTrainingConstant(float mTrainingConstant) {
		this.mTrainingConstant = mTrainingConstant;
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getAccelHistory()
	 */
	@Override
	public String getAccelHistory() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		synchronized(mAccelHistory) {
			for(float[] value : mAccelHistory) {
				jsonObject.accumulate("accel", value[2]);
			}
		}
		return jsonObject.toString();
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getStepCount()
	 */
	@Override
	public int getStepCount() {
		return mStepCount;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setStepCount(int)
	 */
	@Override
	public void setStepCount(int mStepCount) {
		this.mStepCount = mStepCount;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getmPath()
	 */
	@Override
	public ArrayList<float[]> getmPath() {
		return mPath;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setmPath(java.util.ArrayList)
	 */
	@Override
	public void setmPath(ArrayList<float[]> mPath) {
		synchronized(mPath) {
			this.mPath = mPath;
		}
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#pause()
	 */
	@Override
	public void pause() {
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.unregisterCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#resume()
	 */
	@Override
	public void resume() {
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_ACCELEROMETER);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_MAGNETISM);
		mSensorLifecycleManager.registerCallback(this, SensorLifecycleManager.SENSOR_GRAVITY);

	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#restart()
	 */
	@Override
	public void restart() {
		init();
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setAccelThreshold(float)
	 */
	@Override
	public void setAccelThreshold(float mAccelThreshold) {
		this.mAccelThreshold = mAccelThreshold;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getAccelThreshold()
	 */
	@Override
	public float getAccelThreshold() {
		return mAccelThreshold;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setmStartX(float)
	 */
	@Override
	public void setmStartX(float mStartX) {
		this.mStartX = mStartX;
		synchronized (mPath) {
			this.mPath.clear();
			this.mPath.add(new float[]{ this.mStartX, this.mStartY });	
		}
		
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getmStartX()
	 */
	@Override
	public float getmStartX() {
		return mStartX;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setmStartY(float)
	 */
	@Override
	public void setmStartY(float mStartY) {
		this.mStartY = mStartY;
		synchronized (mPath) {
			this.mPath.clear();
			this.mPath.add(new float[]{ this.mStartX, this.mStartY });	
		}
		
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getmStartY()
	 */
	@Override
	public float getmStartY() {
		return mStartY;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getStartPos()
	 */
	@Override
	public float[] getStartPos() {
		return new float[] { getmStartX(), getmStartY() };
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setStartPos(float, float)
	 */
	@Override
	public void setStartPos(float x, float y) {
		setmStartX(x);
		setmStartY(y);
	}
	
	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getmMapWidth()
	 */
	@Override
	public int getmMapWidth() {
		return mMapWidth;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setmMapWidth(double)
	 */
	@Override
	public void setmMapWidth(int mMapWidth) {
		this.mMapWidth = mMapWidth;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#getmMapHeight()
	 */
	@Override
	public int getmMapHeight() {
		return mMapHeight;
	}

	/* (non-Javadoc)
	 * @see in.ernet.iitr.divyeuec.algorithms.IReckoningMethod#setmMapHeight(double)
	 */
	@Override
	public void setmMapHeight(int mMapHeight) {
		this.mMapHeight = mMapHeight;
	}

	@Override
	public String getmPathJSON() {
		synchronized(mPath) {
			try {
				JSONArray pathArray = new JSONArray();
				ArrayList<float[]> path = getmPath();
				
				for(float[] point : path) {
					JSONArray pathPoint = new JSONArray();
					pathPoint.put(point[0]);
					pathPoint.put(point[1]);
					pathArray.put(pathPoint);
				}
				String pathString = pathArray.toString();
				// Log.i(TAG, "Current path: " + pathString);
				return pathString;
			} catch(JSONException e) {
				Log.e(TAG, "Error serializing path to JSON.", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public boolean isLogging() {
		return mIsLogging;
	}
	
	@Override
	public void startLogging() {
		try {
			String logFileBaseName = "drLog." + DateFormat.format("yyyy-MM-dd-hh-mm-ss", new Date(System.currentTimeMillis())).toString();
			mAccelLogFileWriter = new FileWriter(new File(SAMPLES_DIR, logFileBaseName + ".accel.csv"));
			mStepLogFileWriter = new FileWriter(new File(SAMPLES_DIR, logFileBaseName + ".steps.csv"));
		} catch (IOException e) {
			Log.e(TAG, "Creating and opening log files failed!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		mIsLogging = true;
	}
	
	@Override
	public void stopLogging() {
		mIsLogging = false;
		
		try {
			mAccelLogFileWriter.flush();
			mAccelLogFileWriter.close();
			mStepLogFileWriter.flush();
			mStepLogFileWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Flushing and closing log files failed!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

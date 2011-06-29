package in.ernet.iitr.divyeuec.algorithms;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.ui.views.MapView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class ParticleFilteredReckoning extends DeadReckoning {

	private static final double MAX_ACCEPTABLE_TRANSITION_COST = 1e-4;
	private static final int STEP_VARIANCE = 1;

	class Particle {
		private static final String KEY_X = "X";
		private static final String KEY_Y = "Y";
		public double x;
		public double y;
		public double radAngleBias;
		public double stepBias;
		public double weight;
		
		public Particle(double x, double y, double radAngleBias, double stepBias) {
			this.x = x;
			this.y = y;
			this.radAngleBias = radAngleBias;
			this.stepBias = stepBias;
			this.weight = 1;
		}
		
		public Particle(double x, double y) {
			this(x, y,0.,0.);
		}
		
		public Particle clone() {
			Particle cloneParticle = new Particle(x,y,radAngleBias, stepBias);
			// Default weight is 1
			return cloneParticle;
		}
		
		public String toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put(KEY_X, x);
			json.put(KEY_Y, y);
			return json.toString();
		}
	}
	
	private static final int NUM_PARTICLES = 50;
	private static final double ANGLE_VARIANCE = (5.0/180)*Math.PI; // 5 degree variance
	private static final String TAG = "ParticleFilteredReckoning";
	private static final double X_SD = 5*MapView.PIXELS_PER_METER/640.0; // 5px X, Y variations in starting position
	private static final double Y_SD = 5*MapView.PIXELS_PER_METER/480.0; // TODO fix this hardcoding
	private static final double INIT_SD_X = 0.25*MapView.PIXELS_PER_METER/640;
	private static final double INIT_SD_Y = 0.25*MapView.PIXELS_PER_METER/480;
	
	protected Particle[] mParticles;
	protected Bitmap mFloorPlan;
	protected Random mRandom;
	private final int floorPlanHeight;
	private final int floorPlanWidth;
	private FileWriter mNumCandidatesFile;
	
	public ParticleFilteredReckoning(Context ctx) {
		super(ctx);
		mFloorPlan = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.floor);
		mFloorPlan = mFloorPlan.copy(Config.ARGB_8888, true);
		
		// Do a white removal
		floorPlanWidth = mFloorPlan.getWidth();
		floorPlanHeight = mFloorPlan.getHeight();
		for(int x = 0; x < floorPlanWidth; ++x) {
			for(int y = 0; y < floorPlanHeight; ++y) {
				if(mFloorPlan.getPixel(x, y) == Color.WHITE)
					mFloorPlan.setPixel(x, y, Color.WHITE - mFloorPlan.getPixel(x, y));
			}
		}	
	}
	
	@Override
	protected void init() {
		super.init();
		mRandom = new Random();
		mParticles = new Particle[NUM_PARTICLES];
		try {
			if(mNumCandidatesFile != null) {
				mNumCandidatesFile.flush();
				mNumCandidatesFile.close();
				mNumCandidatesFile = null;
			}
		} catch(IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't close numCandidates file!",e);
			throw new RuntimeException(e);
		}
		resetParticles(0.f, 0.f);
	}

	@Override
	public void startLogging() {
		try {
			Date now = new Date(System.currentTimeMillis());
			mNumCandidatesFile = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "pfcandidates." + DateFormat.format("yyyy-MM-dd-kk-mm-ss", now) + ".csv"));
		} catch(IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't create a writer for numCandidates file!",e);
			throw new RuntimeException(e);
		}
		super.startLogging();
	}
	
	@Override
	public void stopLogging() {
		try {
			if(mNumCandidatesFile != null) {
				mNumCandidatesFile.flush();
				mNumCandidatesFile.close();
				mNumCandidatesFile = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't close numCandidates file!",e);
			throw new RuntimeException(e);
		}
		super.stopLogging();
	}
	private void resetParticles(float x, float y) {
		
		for(int i = 0; i < NUM_PARTICLES; ++i) {
			mParticles[i] = new Particle(x + INIT_SD_X*mRandom.nextGaussian(),y + INIT_SD_Y*mRandom.nextGaussian());
		}
	}
	
	@Override
	public void setmStartX(float mStartX) {
		super.setmStartX(mStartX);
		for(int i = 0; i < NUM_PARTICLES; ++i) {
			mParticles[i].x = mStartX + (float)(INIT_SD_X*mRandom.nextGaussian());
		}
	}
	
	@Override
	public void setmStartY(float mStartY) {
		super.setmStartY(mStartY);
		for(int i = 0; i < NUM_PARTICLES; ++i) {
			mParticles[i].y = mStartY + (float)(INIT_SD_Y*mRandom.nextGaussian());
		}
	}
	
	@Override
	public void setStartPos(float x, float y) {
		super.setStartPos(x, y);
	}
	
	@Override
	protected void updateLocation(double stepSize, double radAngle) {
		ArrayList<Particle> transitionStates = new ArrayList<Particle>();
		
		// int mapWidth = getmMapWidth(), mapHeight = getmMapHeight();
		// Log.i(TAG, "mapWidth: " + mapWidth);
		// Log.i(TAG, "mapHeight: " + mapHeight);
		
		final float[] location = getLocation();
		final int floorPlanWidth = mFloorPlan.getWidth();
		final int floorPlanHeight = mFloorPlan.getHeight();
		Log.i(TAG, "location: " + location[0] + ", " + location[1]);
		
		for(int i = 0; i < NUM_PARTICLES; ++i) {
			double anglePerturbation = ANGLE_VARIANCE*mRandom.nextGaussian();
			double stepPerturbation =  (stepSize/25.0)*mRandom.nextGaussian(); // 0;
			double peturbedAngle = radAngle + anglePerturbation + mParticles[i].radAngleBias;
			double peturbedStepSize = stepSize + stepPerturbation + mParticles[i].stepBias;
			
			// TODO Refactor this to get rid of the mMapWidth and mMapHeight constants
			double newParticleX = Math.min(1, Math.max(mParticles[i].x + peturbedStepSize*Math.sin(peturbedAngle)/getmMapWidth(), 0));
			double newParticleY = Math.min(1, Math.max(mParticles[i].y - peturbedStepSize*Math.cos(peturbedAngle)/getmMapHeight(), 0));
			//Log.i(TAG, "newParticleX: " + newParticleX);
			//Log.i(TAG, "newParticleY: " + newParticleY);
			Particle newParticle = new Particle(newParticleX, newParticleY, anglePerturbation, stepPerturbation);
			
			float transitionCost = transitionCost(mParticles[i], newParticle);
			
			if(Math.abs(transitionCost) < 1e-4) {
				// Particle doesn't cross walls
				newParticle.weight = 1 - transitionCost;
				transitionStates.add(newParticle);
			} else {
				// Add with low probability to fix getting "stuck" in map artifacts
				//if(mRandom.nextDouble() < 0.01) {
				//	transitionState.add(newParticles[i]);
				//}
			}
		}

		// Now, pick and replicate samples using weights
		int numCandidates = transitionStates.size();
		Log.d(TAG, "numCandidates: " + numCandidates);
		try {
			if(this.isLogging()) {
				mNumCandidatesFile.write(System.currentTimeMillis() + "," + numCandidates + "\n");
				mNumCandidatesFile.flush();
			}
		} catch(IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Couldn't write to numCandidates file!", e);
			throw new RuntimeException(e);
		}
		
		if(numCandidates > 0) {
			double[] cumulativeWeights = new double[numCandidates];
			double cWeight = 0;
			int index = 0;
			for(Particle p: transitionStates) {
				cumulativeWeights[index++] = cWeight;
				cWeight += p.weight;
			}
			
			for(int i = 0; i < NUM_PARTICLES; ++i) {
				mParticles[i] = transitionStates.get(rouletteWheel(cumulativeWeights, mRandom.nextDouble()*cWeight));
				Particle disturbedParticle = new Particle(mParticles[i].x + INIT_SD_X*mRandom.nextGaussian(), mParticles[i].y + INIT_SD_Y*mRandom.nextGaussian()); // TODO Add the other parameters for the constructor
				if(transitionCost(mParticles[i], disturbedParticle) < MAX_ACCEPTABLE_TRANSITION_COST) {
					mParticles[i] = disturbedParticle;
				}
			}
		} else {
			// Resampling stage: don't go here unless you get lost!!!
			// Retry with lesser accuracy particles
			for(int i = 0; i < NUM_PARTICLES; ++i) {
				Particle disturbedParticle = new Particle(mParticles[i].x + X_SD*mRandom.nextGaussian(), mParticles[i].y + Y_SD*mRandom.nextGaussian());
				if(transitionCost(mParticles[i], disturbedParticle) < MAX_ACCEPTABLE_TRANSITION_COST) {
					mParticles[i] = disturbedParticle;
				}
			}
			updateLocation(stepSize, radAngle);
			return;
			
			// OR: 
			// Create a new set of particles distributed around the current location.
			// resetParticles(location[0], location[1]);
			// Log.d(TAG, "Color at location: " + Color.red(mFloorPlan.getPixel(Math.round(location[0]*mFloorPlan.getWidth()), Math.round(location[1]*mFloorPlan.getHeight()))));
		}
		
		// This is the random particle selection method
		// Particle newLocation = mParticles[mRandom.nextInt(NUM_PARTICLES)];
		
		// This is the particle averaging method
		Particle newLocation = new Particle(0.,0.);
		double netWeight = 0.;
		for(int i = 0; i < mParticles.length; ++i) {
			double weight = mParticles[i].weight;
			weight = 1;
			newLocation.x += weight * mParticles[i].x;
			newLocation.y += weight * mParticles[i].y;
			newLocation.radAngleBias += weight * mParticles[i].radAngleBias;
			newLocation.stepBias += weight * mParticles[i].stepBias;
			netWeight += weight;
		}
		newLocation.x /= (netWeight);
		newLocation.y /= (netWeight);
		newLocation.radAngleBias /= (netWeight);
		newLocation.stepBias /= (netWeight);
		
		//Log.i(TAG, "Setting new location: " + newLocation.x + ", " + newLocation.y);
		setLocation(newLocation.x, newLocation.y);
		//super.updateLocation(stepSize, radAngle);
		
	}
	
	private float transitionCost(Particle particle, Particle newParticle) {
		final double newParticleX = newParticle.x;
		final double newParticleY = newParticle.y;
		final float[] startPos = new float[] { (float) (particle.x*floorPlanWidth), (float) (particle.y*floorPlanHeight) };
		double deltaX = (newParticleX - particle.x)*floorPlanWidth;
		double deltaY = (newParticleY - particle.y)*floorPlanHeight;
		final double numSteps = Math.round(Math.max(Math.abs(deltaX), Math.abs(deltaY)));
		deltaX /= numSteps;
		deltaY /= numSteps;
		
		// Check that no red pixel is crossed
		int maxRed = 0;
		for(int step = 0; step < numSteps; ++step) {
			int x = (int) Math.round(startPos[0] + step*deltaX);
			int y = (int) Math.round(startPos[1] + step*deltaY);
			// Log.d(TAG, "x: " + x + " y: " + y);
			if(x >= 0 && x < floorPlanWidth && y >= 0 && y < floorPlanHeight) {
				int pixel = mFloorPlan.getPixel(x,y);
				maxRed = Math.max(maxRed, Color.red(pixel));
			} else {
				Log.w(TAG, "Particle out of bounds. Unless you're close to the edge of the map, this is serious.");
			}
		}
		
		// Check that there is no red pixel within the 3x3 matrix around the new pixel
		// This is to account for rounding errors in the float values
		int x0 = (int) Math.round(newParticle.x*floorPlanWidth); 
		int y0 = (int) Math.round(newParticle.y*floorPlanHeight);
		for(int xoffset = -1; xoffset <= 1; ++xoffset) {
			for(int yoffset = -1; yoffset <= 1; ++yoffset) {
				int x = x0 + xoffset;
				int y = y0 + yoffset;
				if(x >= 0 && x < floorPlanWidth && y >= 0 && y < floorPlanHeight) {
					int pixel = mFloorPlan.getPixel(x,y);
					maxRed = Math.max(maxRed, Color.red(pixel));
				}
			}
		}
		return maxRed/255.0f;
	}

	// Return the index of element in the cumulativeWeights list that must be selected for the
	// random value d chosen in the range [0, netCumulativeWeight]
	private int rouletteWheel(double[] cumulativeWeights, double d) {
		int start = 0, end = cumulativeWeights.length, mid = start + (end-start)/2;
		if(d < cumulativeWeights[start])
			return start;
		
		while(start < mid) {
			Double compareValue = cumulativeWeights[mid];
			if(d < compareValue) {
				end = mid;
			} else if(compareValue == d) {
				return mid;
			} else {
				start = mid;
			}
			mid = start + (end-start)/2;
		}
		return mid;
	}

	public String getParticles() {
		synchronized (mParticles) {
			JSONArray array = new JSONArray();
			try {
				for(Particle p : mParticles) {
						array.put(p.toJSON());
				}
			} catch (JSONException e) {
				Log.e(TAG, "getParticles failed to serialize to JSON.", e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return array.toString();	
		}
	}
	

}

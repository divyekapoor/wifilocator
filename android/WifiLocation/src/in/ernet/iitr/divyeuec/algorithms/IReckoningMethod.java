package in.ernet.iitr.divyeuec.algorithms;

import java.util.ArrayList;

import org.json.JSONException;

public interface IReckoningMethod {

	public abstract double getStepSize();

	public abstract void setLocation(float[] stepDisplacement);
	public abstract void setLocation(double x, double y);

	public abstract float[] getLocation();
	public abstract String getLocationJSON();

	public abstract double getAngleRadians();

	public abstract double getAngle();

	public abstract float getTrainingConstant();

	public abstract void setTrainingConstant(float mTrainingConstant);

	public abstract String getAccelHistory() throws JSONException;

	public abstract int getStepCount();

	public abstract void setStepCount(int mStepCount);

	public abstract ArrayList<float[]> getmPath();
	
	public abstract String getmPathJSON();

	public abstract void setmPath(ArrayList<float[]> mPath);

	public abstract void pause();

	public abstract void resume();

	public abstract void restart();

	public abstract void setAccelThreshold(float mAccelThreshold);

	public abstract float getAccelThreshold();

	public abstract void setmStartX(float mStartX);

	public abstract float getmStartX();

	public abstract void setmStartY(float mStartY);

	public abstract float getmStartY();

	public abstract float[] getStartPos();

	public abstract void setStartPos(float x, float y);

	public abstract int getmMapWidth();

	public abstract void setmMapWidth(int mMapWidth);

	public abstract int getmMapHeight();

	public abstract void setmMapHeight(int mMapHeight);

	public abstract boolean isLogging();

	public abstract void stopLogging();

	public abstract void startLogging();

}
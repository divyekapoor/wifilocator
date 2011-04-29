package in.ernet.iitr.divyeuec.sensors.internal;

public interface IHWSensorEventCallback {
	void onAccelUpdate(float[] values, long deltaT, long timestamp);
	void onGravityUpdate(float[] values, long deltaT, long timestamp);
	void onMagneticFieldUpdate(float[] values, long deltaT, long timestamp);
	void onGyroUpdate(float[] values, long deltaT, long timestamp);
}

package in.iitr.ernet.divyeuec.util;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorUtilities {
	
	private static final String TAG = "SensorUtilities";

	/**
	 * Log all the Sensors present on the device.
	 */
	public static void LogSensorInformation(Context ctx) {
		SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s: sensorList) {
			StringBuilder sb = new StringBuilder();
			sb.append("Name: ");
			sb.append(s.getName());
			sb.append("Power: ");
			sb.append(s.getPower());
			sb.append("mA ");
			sb.append("Type: ");
			sb.append(s.getType());
			
			switch(s.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				sb.append("Accelerometer");
				break;
			case Sensor.TYPE_GRAVITY:
				sb.append("Gravity");
				break;
			case Sensor.TYPE_GYROSCOPE:
				sb.append("Gyro");
				break;
			case Sensor.TYPE_LIGHT:
				sb.append("Light");
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				sb.append("Magnetic");
				break;
			default:
				sb.append("Unknown");
			}
			Log.i(TAG, sb.toString());
		}

	}

}

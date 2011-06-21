package in.iitr.ernet.divyeuec.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

public class SensorUtilities {
	
	private static final String TAG = "SensorUtilities";

	/**
	 * Log all the Sensors present on the device.
	 */
	public static void LogSensorInformation(Context ctx) {
		FileWriter sensorInfoFileWriter;
		try {
			sensorInfoFileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "sensor_info.txt"));
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
				String sensorInfo = sb.toString();
				Log.i(TAG, sensorInfo);
				sensorInfoFileWriter.write(sensorInfo + "\n");
			}
			
			sensorInfoFileWriter.flush();
			sensorInfoFileWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Sensor info log file could not be created.", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

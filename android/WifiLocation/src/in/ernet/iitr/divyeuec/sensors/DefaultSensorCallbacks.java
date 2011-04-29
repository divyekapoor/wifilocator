package in.ernet.iitr.divyeuec.sensors;

import java.util.List;
import java.util.Map;

/**
 * This class provides default (empty) implementations for the Callbacks required
 * to be implemented for the SensorLifecycleManager.
 * @author Divye Kapoor {divyeuec@iitr.ernet.in}
 *
 */
public class DefaultSensorCallbacks implements ISensorCallback {

	@Override
	public void onAccelUpdate(float[] values, long deltaT, long timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGravityUpdate(float[] values, long deltaT, long timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMagneticFieldUpdate(float[] values, long deltaT,
			long timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGyroUpdate(float[] values, long deltaT, long timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWifiScanResultsAvailable(List<Map<String, String>> scanResults) {
		// TODO Auto-generated method stub
		
	}

}

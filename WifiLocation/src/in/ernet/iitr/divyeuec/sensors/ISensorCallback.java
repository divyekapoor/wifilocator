package in.ernet.iitr.divyeuec.sensors;

import in.ernet.iitr.divyeuec.sensors.internal.IHWSensorEventCallback;
import in.ernet.iitr.divyeuec.sensors.internal.IWifiScanResultsAvailableCallback;

public interface ISensorCallback extends IHWSensorEventCallback, IWifiScanResultsAvailableCallback {
	// Merge the 2 internal interfaces to provide a single interface
	// that any class desirous of registering for hardware + wifi sensor events must support.
	
	// Default (empty) implementations of the functions are provided in the
	// DefaultSensorCallbacks class.
}

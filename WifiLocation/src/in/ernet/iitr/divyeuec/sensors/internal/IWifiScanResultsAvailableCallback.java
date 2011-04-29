package in.ernet.iitr.divyeuec.sensors.internal;

import java.util.List;
import java.util.Map;

public interface IWifiScanResultsAvailableCallback {
	public void onWifiScanResultsAvailable(List<Map<String,String>> scanResults);
}

package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;

import android.util.Pair;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.util.SetUtils;

public class ClampedRSSIDistanceAlgorithm implements
		ILocationFingerprintDistanceAlgorithm {

	private static final Float MIN_RSSI_VALUE_DBM = -92.f;
	private static final Float DEFAULT_RSSI_TOLERANCE = 5.0f; // 5 dbm

	@Override
	public double distance(LocationFingerprint my, LocationFingerprint other) {
		JSONArray myMacAddrArray = my.getmMacAddrArray();
		JSONArray otherMacAddrArray = other.getmMacAddrArray();
		
		TreeSet<String> myMacAddrs = DistanceUtils.MACAddrArraytoTreeSet(myMacAddrArray);
		TreeSet<String> otherMacAddrs = DistanceUtils.MACAddrArraytoTreeSet(otherMacAddrArray);
		
		List<String> commonAPs = SetUtils.intersect(myMacAddrs, otherMacAddrs);
		int numMatch = commonAPs.size();
		// Log.d(TAG, "Number of samples matched: " + numMatch);
		if(numMatch == 0)
			return Double.POSITIVE_INFINITY; // Shortcut to prevent large processing
		
		Pair<List<String>, List<String>> differAPs = SetUtils.difference(myMacAddrs, otherMacAddrs); 
		
		float error = 0;
		for(String AP : commonAPs) {
			Float rssi1 = my.getmAPRSSI(AP);
			Float rssi2 = other.getmAPRSSI(AP);
			
			// Consider a match if it is within the RSSI Tolerance limit
			if(Math.abs(rssi1 - rssi2) > DEFAULT_RSSI_TOLERANCE)
				error += ((rssi1-rssi2-DEFAULT_RSSI_TOLERANCE)*(rssi1 - rssi2-DEFAULT_RSSI_TOLERANCE))/(DEFAULT_RSSI_TOLERANCE*DEFAULT_RSSI_TOLERANCE);
		}
		
		for(String AP : differAPs.first) {
			Float rssi = my.getmAPRSSI(AP);
			float difference = (rssi - MIN_RSSI_VALUE_DBM - DEFAULT_RSSI_TOLERANCE);
			difference = Math.max(difference, 0);
			
			error += difference*difference/(DEFAULT_RSSI_TOLERANCE*DEFAULT_RSSI_TOLERANCE);
		}
		
		for(String AP : differAPs.second) {
			Float rssi = other.getmAPRSSI(AP);
			float difference = (rssi - MIN_RSSI_VALUE_DBM - DEFAULT_RSSI_TOLERANCE);
			difference = Math.max(difference, 0);
			
			error += difference*difference/(DEFAULT_RSSI_TOLERANCE*DEFAULT_RSSI_TOLERANCE);
		}
		
		return Math.sqrt((double)error);

	}

}

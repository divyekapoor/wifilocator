package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.util.SetUtils;

import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import android.util.Pair;

public class RSSIDistanceAlgorithm implements ILocationFingerprintDistanceAlgorithm {
	private static final String TAG = "LocationFingerprintDistanceAlgorithm";
	public static final int MIN_RSSI_VALUE_DBM = -92;
	
	public double distance(LocationFingerprint my, LocationFingerprint other) {
		// Euclidean distance between fingerprints
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
			error += (rssi1-rssi2)*(rssi1 - rssi2);
		}
		
		for(String AP : differAPs.first) {
			Float rssi = my.getmAPRSSI(AP);
			error += (rssi - MIN_RSSI_VALUE_DBM)*(rssi - MIN_RSSI_VALUE_DBM);
		}
		
		for(String AP : differAPs.second) {
			Float rssi = other.getmAPRSSI(AP);
			error += (rssi - MIN_RSSI_VALUE_DBM)*(rssi - MIN_RSSI_VALUE_DBM);
		}
		
		return Math.sqrt((double)error);

	}
}

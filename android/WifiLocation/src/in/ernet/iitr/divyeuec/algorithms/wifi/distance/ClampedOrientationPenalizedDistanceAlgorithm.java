package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;

import android.util.Pair;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.util.SetUtils;

public class ClampedOrientationPenalizedDistanceAlgorithm implements
		ILocationFingerprintDistanceAlgorithm {

	private static final int BASE_TOLERANCE = 2;
	private static final float ANGLES_PER_UNIT_ERROR = 60;
	private static final Float MIN_RSSI_VALUE_DBM = -92.f;

	private double getClampingValue(float angle1, float angle2) {
		if(angle1 > angle2) {
			float tmp = angle1;
			angle1 = angle2;
			angle2 = tmp;
		}
		return BASE_TOLERANCE + Math.min(angle2-angle1, angle2-angle1+360.f)/ANGLES_PER_UNIT_ERROR;
	}
	
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
		double tolerance = getClampingValue(my.getmAngle(), other.getmAngle());
		for(String AP : commonAPs) {
			Float rssi1 = my.getmAPRSSI(AP);
			Float rssi2 = other.getmAPRSSI(AP);
			
			// Consider a perfect match if it is within the RSSI Tolerance limit.
			// Else add an error term
			if(Math.abs(rssi1 - rssi2) > tolerance)
				error += ((rssi1-rssi2-tolerance)*(rssi1 - rssi2-tolerance))/(tolerance*tolerance);
		}
		
		for(String AP : differAPs.first) {
			Float rssi = my.getmAPRSSI(AP);
			double difference = (rssi - MIN_RSSI_VALUE_DBM - tolerance)/tolerance;
			difference = Math.max(difference, 0);
			
			error += difference*difference;
		}
		
		for(String AP : differAPs.second) {
			Float rssi = other.getmAPRSSI(AP);
			double difference = (rssi - MIN_RSSI_VALUE_DBM - tolerance)/tolerance;
			difference = Math.max(difference, 0);
			
			error += difference*difference;
		}
		
		return Math.sqrt((double)error);
	}

}

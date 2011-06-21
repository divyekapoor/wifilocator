package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.util.SetUtils;

import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;

import android.util.Pair;

public class OrientedRSSIDistanceAlgorithm implements
		ILocationFingerprintDistanceAlgorithm {

	private static final float ANGLE_TOLERANCE = 60.f;

	@Override
	public double distance(LocationFingerprint my, LocationFingerprint other) {
		// Skip samples that don't match our orientation.
		// Ensure angle1 < angle2
		float angle1 = my.getmAngle();
		float angle2 = other.getmAngle();
		if(angle1 > angle2) {
			float tmp = angle1;
			angle1 = angle2;
			angle2 = tmp;
		}
		
		float difference = Math.min(angle2 - angle1, angle2 - angle1 + 360.f);
		// If they are of the same sign, subtract and decide
		// If they are of opposite signs, then try both methods
		
		if(difference > ANGLE_TOLERANCE)
			return Double.POSITIVE_INFINITY;
		
		JSONArray myMacAddrArray = my.getmMacAddrArray();
		JSONArray otherMacAddrArray = other.getmMacAddrArray();
		
		TreeSet<String> myMacAddrs = DistanceUtils.MACAddrArraytoTreeSet(myMacAddrArray);
		TreeSet<String> otherMacAddrs = DistanceUtils.MACAddrArraytoTreeSet(otherMacAddrArray);
		
		List<String> commonAPs = SetUtils.intersect(myMacAddrs, otherMacAddrs);
		if(commonAPs.size() == 0)
			return Double.POSITIVE_INFINITY; // shortcut distance computation
		
		Pair<List<String>, List<String>> differAPs = SetUtils.difference(myMacAddrs, otherMacAddrs);

		float error = 0;
		for(String AP : commonAPs) {
			Float rssi1 = my.getmAPRSSI(AP);
			Float rssi2 = other.getmAPRSSI(AP);
			error += (rssi1-rssi2)*(rssi1 - rssi2);
		}
		
		for(String AP : differAPs.first) {
			Float rssi = my.getmAPRSSI(AP);
			error += (rssi - RSSIDistanceAlgorithm.MIN_RSSI_VALUE_DBM)*(rssi - RSSIDistanceAlgorithm.MIN_RSSI_VALUE_DBM);
		}
		
		for(String AP : differAPs.second) {
			Float rssi = other.getmAPRSSI(AP);
			error += (rssi - RSSIDistanceAlgorithm.MIN_RSSI_VALUE_DBM)*(rssi - RSSIDistanceAlgorithm.MIN_RSSI_VALUE_DBM);
		}
		
		return Math.sqrt((double)error);
		
	}

}

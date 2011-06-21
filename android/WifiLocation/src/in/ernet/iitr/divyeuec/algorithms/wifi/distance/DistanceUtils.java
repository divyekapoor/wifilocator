package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class DistanceUtils {
	private static final String TAG = "DistanceUtils";

	public static TreeSet<String> MACAddrArraytoTreeSet(JSONArray myMacAddrArray) {
		TreeSet<String> array1 = new TreeSet<String>();
		
		try {
			for(int i = 0; i < myMacAddrArray.length(); ++i) {
				array1.add(myMacAddrArray.getString(i));
			}
		} catch(JSONException e) {
			Log.e(TAG, "Bug in Mac Address serialized file?", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return array1;
	}
}

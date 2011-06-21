package in.iitr.ernet.divyeuec.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import android.util.Pair;

public class SetUtils {
	
	private static final String TAG = "SetUtils";

	public static List<String> intersect(TreeSet<String> myMacAddrs,
			TreeSet<String> otherMacAddrs) {
		
		ArrayList<String> common = new ArrayList<String>();
		String s1 = null, s2 = null;
		boolean first = true;
		for(Iterator<String> it1 = myMacAddrs.iterator(), it2 = otherMacAddrs.iterator(); it1.hasNext() && it2.hasNext(); ) {
			if(first) {
				s1 = it1.next();
				s2 = it2.next();
				first = false;
			}
			
			int compareVal = s1.compareTo(s2);
			if(compareVal == 0) {
				common.add(s1);
				if(it1.hasNext()) {
					s1 = it1.next();
				} else {
					break;
				}
				
				if(it2.hasNext()) {
					s2 = it2.next();
				} else {
					break;
				}
				
			} else if(compareVal > 0) {
				if(it2.hasNext()) {
					s2 = it2.next();
				} else {
					break;
				}
			} else {
				if(it1.hasNext()) {
					s1 = it1.next();
				} else {
					break;
				}
			}
		}
		
		return common;
	}
	
	public static Pair<List<String>, List<String>> difference(
			TreeSet<String> myMacAddrs, TreeSet<String> otherMacAddrs) {
		
		ArrayList<String> leftDifference = leftDifference(myMacAddrs, otherMacAddrs);
		ArrayList<String> rightDifference = leftDifference(otherMacAddrs, myMacAddrs);
		
		return new Pair<List<String>, List<String>>(leftDifference, rightDifference);
	}

	public static ArrayList<String> leftDifference(TreeSet<String> myMacAddrs,
			TreeSet<String> otherMacAddrs) {
		ArrayList<String> leftDifference = new ArrayList<String>();
		
		for(String s: myMacAddrs) {
			if(!otherMacAddrs.contains(s))
				leftDifference.add(s);
		}
		
		return leftDifference;
	}


}

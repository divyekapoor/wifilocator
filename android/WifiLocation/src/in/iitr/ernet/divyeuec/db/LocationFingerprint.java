package in.iitr.ernet.divyeuec.db;

import in.ernet.iitr.divyeuec.sensors.WifiScanResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.Pair;

public class LocationFingerprint {
	private static final int MIN_RSSI_VALUE_DBM = -92;
	public static final int RAVINDRA_BHAWAN_MAP_ID = 1;
	private static final String TAG = "LocationFingerprint";
	
	// Keys to values in the JSON object
	public static final String KEY_MAP_ID = "MapId";
	public static final String KEY_SAMPLE_NAME = "SampleName";
	public static final String KEY_SAMPLE_X = "X";
	public static final String KEY_SAMPLE_Y = "Y";
	public static final String KEY_SAMPLE_ANGLE = "Angle";
	public static final String KEY_SAMPLE_TIME = "TimeMs";
	public static final String KEY_SAMPLE_TIME_HUMAN_READABLE = "HumanTime";
	public static final String KEY_AP_FREQUENCY = "APFrequency";
	public static final String KEY_AP_CAPABILITIES = "APCapabilities";
	public static final String KEY_AP_SSID = "APSSID";
	public static final String KEY_AP_RSSI_DBM = "APRSSI";
	public static final String KEY_MAC_ADDRESSES_ARRAY = "MACAddresses";
	
	private JSONObject mFingerprintData;
	
	
	private void init(int mapId, Date sampleTime, float angle, float X,
			float Y, List<Map<String,String>> wifiSample, String prefix) throws JSONException {
		mFingerprintData = serialize(mapId, sampleTime, angle, X, Y, wifiSample, prefix);
	}

	public LocationFingerprint(int mapId, Date sampleTime, float angle,
			float X, float Y, List<Map<String,String>> wifiSample, String prefix) {
		try {
			init(mapId, sampleTime, angle, X, Y, wifiSample, prefix);
		} catch(JSONException e) {
			Log.e(TAG, "Construction of LocationFingerprint failed", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public LocationFingerprint(File f) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(f));
			StringBuilder sb = new StringBuilder();
			String line = bufferedReader.readLine();
			while(line != null) {
				sb.append(line);
				line = bufferedReader.readLine();
			}
			JSONObject locationFingerprintJSON = new JSONObject(sb.toString());
			deserialize(locationFingerprintJSON);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch(IOException e) {
			Log.e(TAG, "IOException!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (JSONException e) {
			Log.e(TAG, "JSON exception in LocationFingerprint(File f)", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public float distance(LocationFingerprint other) {
		// Euclidean distance between fingerprints
		JSONArray myMacAddrArray = getmMacAddrArray();
		JSONArray otherMacAddrArray = other.getmMacAddrArray();
		
		TreeSet<String> myMacAddrs = toTreeSet(myMacAddrArray);
		TreeSet<String> otherMacAddrs = toTreeSet(otherMacAddrArray);
		
		List<String> commonAPs = intersect(myMacAddrs, otherMacAddrs);
		Pair<List<String>, List<String>> differAPs = difference(myMacAddrs, otherMacAddrs); 
		
		int numMatch = commonAPs.size();
		Log.d(TAG, "Number of samples matched: " + numMatch);
		float error = 0;
		for(String AP : commonAPs) {
			Float rssi1 = getmAPRSSI(AP);
			Float rssi2 = other.getmAPRSSI(AP);
			error += (rssi1-rssi2)*(rssi1 - rssi2);
		}
		
		for(String AP : differAPs.first) {
			Float rssi = getmAPRSSI(AP);
			error += (rssi - MIN_RSSI_VALUE_DBM)*(rssi - MIN_RSSI_VALUE_DBM);
		}
		
		for(String AP : differAPs.second) {
			Float rssi = other.getmAPRSSI(AP);
			error += (rssi - MIN_RSSI_VALUE_DBM)*(rssi - MIN_RSSI_VALUE_DBM);
		}
		
		return (float)Math.sqrt((double)error);
		
	}

	private Pair<List<String>, List<String>> difference(
			TreeSet<String> myMacAddrs, TreeSet<String> otherMacAddrs) {
		
		ArrayList<String> leftDifference = leftDifference(myMacAddrs, otherMacAddrs);
		ArrayList<String> rightDifference = leftDifference(otherMacAddrs, myMacAddrs);
		
		return new Pair<List<String>, List<String>>(leftDifference, rightDifference);
	}

	private ArrayList<String> leftDifference(TreeSet<String> myMacAddrs,
			TreeSet<String> otherMacAddrs) {
		ArrayList<String> leftDifference = new ArrayList<String>();
		
		for(String s: myMacAddrs) {
			if(!otherMacAddrs.contains(s))
				leftDifference.add(s);
		}
		
		return leftDifference;
	}

	private List<String> intersect(TreeSet<String> myMacAddrs,
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
				s1 = it1.next();
				s2 = it2.next();
			} else if(compareVal > 0) {
				s2 = it2.next();
			} else {
				s1 = it1.next();
			}
		}
		
		return common;
	}

	private TreeSet<String> toTreeSet(JSONArray myMacAddrArray) {
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

	@Override
	public String toString() {
		return getmFingerprintData().toString();
	}
	
	private JSONObject serialize(int mapId, Date sampleTime, float angle,
			float x, float y, List<Map<String, String>> wifiSample,
			String prefix) throws JSONException {
		JSONObject serializedSample = new JSONObject();
		serializedSample.put(KEY_MAP_ID, mapId);
		serializedSample.put(KEY_SAMPLE_NAME, prefix);
		serializedSample.put(KEY_SAMPLE_X, x);
		serializedSample.put(KEY_SAMPLE_Y, y);
		serializedSample.put(KEY_SAMPLE_ANGLE, angle);
		serializedSample.put(KEY_SAMPLE_TIME, sampleTime.getTime());
		serializedSample.put(KEY_SAMPLE_TIME_HUMAN_READABLE, sampleTime.toString() + " " + sampleTime.getHours() + ":" + sampleTime.getMinutes() + ":" + sampleTime.getSeconds());
		
		// Get Mac Addresses
		ArrayList<String> macAddresses = new ArrayList<String>();
		for(Map<String,String> scanResult: wifiSample) {
			macAddresses.add(scanResult.get(WifiScanResults.KEY_BSSID));
			
			JSONObject apInfo = new JSONObject();
			apInfo.put(KEY_AP_FREQUENCY, scanResult.get(WifiScanResults.KEY_FREQUENCY));
			apInfo.put(KEY_AP_CAPABILITIES, scanResult.get(WifiScanResults.KEY_CAPABILITIES));
			apInfo.put(KEY_AP_SSID, scanResult.get(WifiScanResults.KEY_SSID));
			apInfo.put(KEY_AP_RSSI_DBM, scanResult.get(WifiScanResults.KEY_RSSI_DBM));
	
			serializedSample.put(scanResult.get(WifiScanResults.KEY_BSSID), apInfo);
		}
		
		serializedSample.put(KEY_MAC_ADDRESSES_ARRAY, new JSONArray(macAddresses));
		
		Log.i(TAG, serializedSample.toString());
		
		return serializedSample;
	}

	private void deserialize(JSONObject locationFingerprintJSON) {
		setmFingerprintData(locationFingerprintJSON);
	}

	public boolean save(IFingerprintDB db) {
		return db.persist(this);
	}
	
	public int getmMapId()  {
		try {
			return mFingerprintData.getInt(KEY_MAP_ID);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmMapId(int mMapId) {
		try {
			mFingerprintData.put(KEY_MAP_ID, mMapId);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Date getmSampleTime() {
		try {
			return new Date(mFingerprintData.getLong(KEY_SAMPLE_TIME));
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmSampleTime(long mSampleTime) {
		try {
			mFingerprintData.put(KEY_SAMPLE_TIME, mSampleTime);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public float getmAngle() {
		try {
			return (float) mFingerprintData.getDouble(KEY_SAMPLE_ANGLE);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmAngle(float mAngle) {
		try {
			mFingerprintData.put(KEY_SAMPLE_ANGLE, mAngle);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public float getmX() {
		try {
			return (float) mFingerprintData.getDouble(KEY_SAMPLE_X);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmX(float mX) {
		try {
			mFingerprintData.put(KEY_SAMPLE_X, mX);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public float getmY() {
		try {
			return (float) mFingerprintData.getDouble(KEY_SAMPLE_Y);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmY(float mY) {
		try {
			mFingerprintData.put(KEY_SAMPLE_Y, mY);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public JSONObject getmFingerprintData() {
		return mFingerprintData;
	}

	public void setmFingerprintData(JSONObject mFingerprintData) {
		this.mFingerprintData = mFingerprintData;
	}

	public String getmPrefix() {
		try {
			return mFingerprintData.getString(KEY_SAMPLE_NAME);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmPrefix(String mPrefix) {
		try {
			mFingerprintData.put(KEY_SAMPLE_NAME, mPrefix);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public JSONArray getmMacAddrArray() {
		try {
			return mFingerprintData.getJSONArray(KEY_MAC_ADDRESSES_ARRAY);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmMacAddrArray(JSONArray macAddrArray) {
		try {
			mFingerprintData.put(KEY_MAC_ADDRESSES_ARRAY, macAddrArray);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid Key!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Float getmAPRSSI(String macAddr) {
		try {
			JSONObject APdata = getmFingerprintData().getJSONObject(macAddr);
			return Float.parseFloat(APdata.getString(KEY_AP_RSSI_DBM));
		} catch(JSONException e) {
			return null;
		}
	}
}

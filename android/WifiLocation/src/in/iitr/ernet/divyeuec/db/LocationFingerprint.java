package in.iitr.ernet.divyeuec.db;

import in.ernet.iitr.divyeuec.sensors.WifiScanResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LocationFingerprint {
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
	
	
	public LocationFingerprint(JSONObject fingerprintData) {
		// Note: The data is trusted and not verified. So be prepared for exceptions
		deserialize(fingerprintData);
	}

	@Override
	public String toString() {
		return getmFingerprintData().toString();
	}
	
	/**
	 * Note: Use this function with care to get a JSON representation of the Location fingerprint.
	 * @param mapId The map with which this fingerprint is associated
	 * @param sampleTime The time at which the fingerprint was taken
	 * @param angle The azimuth orientation angle at which the sample was taken
	 * @param x The X location of the sample point on the map (0-1 coordinate system)
	 * @param y The Y location of the sample point on the map (0-1 coordinate system)
	 * @param wifiSample The actual Wifi Sample received
	 * @param prefix Any sample name
	 * @return JSON representation of the fingerprint with these details included.
	 * @throws JSONException
	 */
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

	public String getmSampleName() {
		try {
			return mFingerprintData.getString(KEY_SAMPLE_NAME);
		} catch (JSONException e) {
			Log.e(TAG, "Key Missing!", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setmSampleName(String mPrefix) {
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

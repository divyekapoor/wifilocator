package in.iitr.ernet.divyeuec.db;

import in.ernet.iitr.divyeuec.ui.views.MapView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class SQLiteDB implements IFingerprintDB {

	private static final String DATABASE_NAME = "FingerprintDB.sqlite";
	private static final String TABLE_NAME = "FingerprintDB";
	private static final String COL_ID = "_id";
	private static final String COL_MAPID = "mapId";
	private static final String COL_X = "x";
	private static final String COL_Y = "y";
	private static final String COL_ANGLE = "angle";
	private static final String COL_DATETIME = "dt";
	private static final String COL_FINGERPRINTJSON = "fingerprintJSON";
	private static final String COL_SAMPLENAME = "sampleName";
	private static final String TAG = "SQLiteDB";
	private static final int KNN_LIMIT = 7;
	private static final String SQL_DROP_TABLES = "drop table " + TABLE_NAME + ";";
	private static final String SQL_CREATE_TABLES = "create table " + TABLE_NAME + 
	"( " + COL_ID + " integer primary key autoincrement, " +
	"  " + COL_MAPID + " integer not null, " +
	"  " + COL_X + " double not null, " +
	"  " + COL_Y + " double not null, " +
	"  " + COL_ANGLE + " double not null, " +
	"  " + COL_DATETIME + " datetime not null, " +
	"  " + COL_SAMPLENAME + " varchar(1024) not null, " +
	"  " + COL_FINGERPRINTJSON + " text not null);";
	
	
	private static class FingerprintDBOpenHelper extends SQLiteOpenHelper {
		private static final String TAG = "FingerprintDBOpenHelper";
		private static final int DATABASE_VERSION = 3;

		public FingerprintDBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading SQLite DB from version " + oldVersion + " to " + newVersion + ". Data loss WILL result.");
			db.execSQL(SQL_DROP_TABLES);
			onCreate(db);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}
		
	}

	private static SQLiteDB mInstance;
	
	private SQLiteDatabase mFingerprintDb;
	private FingerprintDBOpenHelper mFingerprintDBOpenHelper;
	private SQLiteStatement mInsertFingerprint;
	private List<LocationFingerprint> mAllSamples;
	private Context mCtx;
	
	private SQLiteDB(Context context) {
		mCtx = context;
		mFingerprintDBOpenHelper = new FingerprintDBOpenHelper(context);
		mFingerprintDb = mFingerprintDBOpenHelper.getWritableDatabase();
		mInsertFingerprint = mFingerprintDb.compileStatement("insert into " + TABLE_NAME + "(" + COL_MAPID + "," + COL_X + "," + COL_Y + "," + COL_ANGLE + "," + COL_DATETIME + "," + COL_SAMPLENAME + "," + COL_FINGERPRINTJSON + ") " + " values (?, ?, ?, ?, ?, ?, ?)");
		
		try {
			FileWriter dbPathLogger = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "dbpath.txt"));
			dbPathLogger.write(mFingerprintDb.getPath());
			dbPathLogger.append('\n');
			dbPathLogger.flush();
			dbPathLogger.close();
		} catch(IOException e) {
			e.printStackTrace();
			Log.e(TAG, "data directory log failed!", e);
			throw new RuntimeException(e);
		}
	}
	
	public static SQLiteDB getInstance(Context ctx) {
		if(mInstance == null) {
			mInstance = new SQLiteDB(ctx.getApplicationContext());
		}
		return mInstance;
	}
	
	@Override
	public boolean persist(LocationFingerprint locationFingerprint) {		
		mInsertFingerprint.clearBindings();
		mInsertFingerprint.bindLong(1, locationFingerprint.getmMapId()); 
		mInsertFingerprint.bindDouble(2, locationFingerprint.getmX());
		mInsertFingerprint.bindDouble(3, locationFingerprint.getmY());
		mInsertFingerprint.bindDouble(4, locationFingerprint.getmAngle());
		mInsertFingerprint.bindString(5, DateFormat.format("yyyy-MM-dd kk:mm:ss", locationFingerprint.getmSampleTime()).toString());
		mInsertFingerprint.bindString(6, locationFingerprint.getmSampleName());
		mInsertFingerprint.bindString(7, locationFingerprint.getmFingerprintData().toString());
		
		return (mInsertFingerprint.executeInsert() != -1);
	}

	@Override
	public LocationFingerprint query(int id) {
		Cursor fingerprintCursor = mFingerprintDb.query(TABLE_NAME, new String[]{COL_FINGERPRINTJSON}, COL_ID + "=" + id, null, null, null, null);
		
		LocationFingerprint lf = null;
		try {
			lf = new LocationFingerprint(new JSONObject(fingerprintCursor.getString(fingerprintCursor.getColumnIndex(COL_FINGERPRINTJSON))));
		} catch(JSONException e) {
			Log.e(TAG, "Location fingerprint with id " + id + " not found.", e);
		} finally {
			fingerprintCursor.close();
		}
		return lf;
	}
	
	// TODO Buggy code. Doesn't seem to work correctly
	@Override
	public List<LocationFingerprint> query(Float X, Float Y, Float angle) {
		final double xTolerance = 3.2*MapView.PIXELS_PER_METER/640.; // 2.5 metres tolerance on either side
		final double yTolerance = 3.2*MapView.PIXELS_PER_METER/480.;
		final double angleTolerance = 90.; // 20 degree tolerance in angles
		
		final double angleLowerLimit = angle - angleTolerance;
		final double angleUpperLimit = angle + angleTolerance;
		
		String angleConstraint = null;
		
		if(angleLowerLimit >= -180 && angleUpperLimit <= 180) {
			angleConstraint = COL_ANGLE + " >= " + angleLowerLimit + " and " + COL_ANGLE + " <= " + angleUpperLimit; 
		} else if(angleLowerLimit < -180 && angleUpperLimit <= 180) {
			angleConstraint = COL_ANGLE + " <= " + angleUpperLimit + " and " + COL_ANGLE + " >= " + (angleLowerLimit + 360);
		} else if(angleLowerLimit >= -180 && angleUpperLimit > 180) {
			angleConstraint = COL_ANGLE + " >= " + angleLowerLimit + " and " + COL_ANGLE + " <= " + (angleUpperLimit - 360);
		} else {
			// Complete range is encompassed
			angleConstraint = "1";
		}
		
		ArrayList<LocationFingerprint> nearbyFingerprints = new ArrayList<LocationFingerprint>();
		Cursor nearbyFingerprintCursor = mFingerprintDb.query(TABLE_NAME, new String[]{COL_FINGERPRINTJSON}, angleConstraint + " and " + COL_X + " > ? and " + COL_X + "< ? and " + COL_Y + " > ? and " + COL_Y + " < ? and " + COL_ANGLE + " > ? ", new String[] { "" + (X - xTolerance), "" + (X + xTolerance), "" + (Y - yTolerance), "" + (Y + yTolerance)}, null, null, null, null);
		//Cursor nearbyFingerprintCursor = mFingerprintDb.query(TABLE_NAME, new String[]{COL_FINGERPRINTJSON},  COL_X + " >= " + (X - xTolerance) + " and " + COL_X + " <= " + (X + xTolerance) + " and " + COL_Y + " >= " + (Y-yTolerance) + " and " + COL_Y + " <= " + (Y + yTolerance), null, null, null, null, null);
		Toast.makeText(mCtx, "Count: " + nearbyFingerprintCursor.getCount(), Toast.LENGTH_SHORT).show();
		int fingerprintColumn = nearbyFingerprintCursor.getColumnIndex(COL_FINGERPRINTJSON);
		if(!nearbyFingerprintCursor.moveToFirst()) {
			return nearbyFingerprints;
		}
			
		do {
			try {
				nearbyFingerprints.add(new LocationFingerprint(new JSONObject(nearbyFingerprintCursor.getString(fingerprintColumn))));
			} catch (JSONException e) {
				Log.e(TAG, "BUGCHECK - the JSON from the database couldn't be deserialized!", e);
				e.printStackTrace();
				nearbyFingerprintCursor.close();
				throw new RuntimeException(e);
			}
		} while(nearbyFingerprintCursor.moveToNext());
		
		nearbyFingerprintCursor.close();
		return nearbyFingerprints;
	}

	@Override
	public List<LocationFingerprint> getAllSamples() {
		// NOTE: This function is a memory hog!!! Be aware!
		if(mAllSamples == null) {
			mAllSamples = new ArrayList<LocationFingerprint>();
			Cursor allSamplesCursor = mFingerprintDb.query(TABLE_NAME, new String[]{COL_FINGERPRINTJSON}, null, null, null, null, null, null);
			int fingerprintColIndex = allSamplesCursor.getColumnIndex(COL_FINGERPRINTJSON);
			if(!allSamplesCursor.moveToFirst()) 
				return null;
			
			do {
				LocationFingerprint locationFingerprint;
				try {
					locationFingerprint = new LocationFingerprint(new JSONObject(allSamplesCursor.getString(fingerprintColIndex)));
				} catch (JSONException e) {
					Log.e(TAG, "BUGCHECK - JSON retrieval from database failed!", e);
					e.printStackTrace();
					allSamplesCursor.close();
					throw new RuntimeException(e);
				}
				mAllSamples.add(locationFingerprint);
			} while(allSamplesCursor.moveToNext());
			allSamplesCursor.close();
		}
		return mAllSamples;
	}
	
	public String getPath() {
		return mFingerprintDb.getPath();
	}
	
	@Override
	public void close() {
		mFingerprintDb.close();
		mFingerprintDBOpenHelper.close();
	}
	
	@Override
	public boolean resetDB() {
		// Warning: Will destroy all data!!!
		boolean result = true;
		try {
			mFingerprintDb.execSQL(SQL_DROP_TABLES);
			mFingerprintDb.execSQL(SQL_CREATE_TABLES);
		} catch (SQLException e) {
			Log.e(TAG, "Couldn't reset db!", e);
			result = false;
			throw new RuntimeException(e);
		}
		
		return result;
	}
}
		
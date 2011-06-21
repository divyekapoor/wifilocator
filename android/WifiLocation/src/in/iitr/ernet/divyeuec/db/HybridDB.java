package in.iitr.ernet.divyeuec.db;

import java.util.List;

import android.content.Context;

public class HybridDB implements IFingerprintDB {

	private static HybridDB mInstance;
	private FileDB mFileDB;
	private SQLiteDB mSqliteDB;

	private HybridDB(Context ctx) {
		mFileDB = FileDB.getInstance(ctx);
		mSqliteDB = SQLiteDB.getInstance(ctx);
	}
	
	public static HybridDB getInstance(Context ctx) {
		if(mInstance == null) {
			mInstance = new HybridDB(ctx);
		}
		return mInstance;
	}
	
	@Override
	public boolean persist(LocationFingerprint locationFingerprint) {
		return mFileDB.persist(locationFingerprint) && 
			mSqliteDB.persist(locationFingerprint);
		
	}

	@Override
	public LocationFingerprint query(int id) {
		return mSqliteDB.query(id);
	}

	@Override
	public List<LocationFingerprint> query(Float X, Float Y, Float angle) {
		return mSqliteDB.query(X, Y, angle);
	}

	@Override
	public List<LocationFingerprint> getAllSamples() {
		return mFileDB.getAllSamples();
	}

	@Override
	public void close() {
		mFileDB.close();
		mSqliteDB.close();
	}
	
	@Override
	public boolean resetDB() {
		return mSqliteDB.resetDB() && mFileDB.resetDB();
	}

}

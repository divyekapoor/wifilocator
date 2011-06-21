package in.iitr.ernet.divyeuec.db;

import java.util.List;

public interface IFingerprintDB {
	public boolean resetDB();
	public boolean persist(LocationFingerprint locationFingerprint);
	public LocationFingerprint query(int id);
	public List<LocationFingerprint> query(Float X, Float Y, Float angle);
	public List<LocationFingerprint> getAllSamples();
	public void close();
}
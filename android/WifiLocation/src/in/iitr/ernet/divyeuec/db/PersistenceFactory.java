package in.iitr.ernet.divyeuec.db;

public class PersistenceFactory {
	public static IFingerprintDB getInstance() {
		return FileDB.getInstance();
	}
}

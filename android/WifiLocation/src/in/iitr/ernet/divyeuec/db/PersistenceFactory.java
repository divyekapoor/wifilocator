package in.iitr.ernet.divyeuec.db;

import android.content.Context;

public class PersistenceFactory {
	public static IFingerprintDB getInstance(Context ctx) {
		return HybridDB.getInstance(ctx);
	}
}

package in.ernet.iitr.divyeuec.algorithms.wifi.distance;

import in.iitr.ernet.divyeuec.db.LocationFingerprint;

public interface ILocationFingerprintDistanceAlgorithm {
	public double distance(LocationFingerprint lhs, LocationFingerprint rhs);
}

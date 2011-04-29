package in.ernet.iitr.divyeuec.algorithms;

public interface IAlgorithm {
	// In case you are using realtime sensors, make sure your algorithm supports stopping itself.
	public void resume();
	public void pause();
}

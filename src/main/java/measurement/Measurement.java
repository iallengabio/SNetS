package measurement;

/**
 * This class represents the performance metrics used in the simulations.
 * 
 * @author Iallen
 */
public abstract class Measurement {
	
	protected int loadPoint;
	protected int replication;
	
	/**
	 * Creates a new instance of Measurement
	 * 
	 * @param loadPoint int
	 * @param replication int
	 */
	public Measurement(int loadPoint, int replication) {
		super();
		this.loadPoint = loadPoint;
		this.replication = replication;
	}

	/**
	 * Returns the load point
	 * 
	 * @return int
	 */
	public int getLoadPoint() {
		return loadPoint;
	}

	/**
	 * Returns the replication
	 * 
	 * @return int
	 */
	public int getReplication() {
		return replication;
	}
	
}

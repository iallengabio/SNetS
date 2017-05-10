package measurement;

public abstract class Measurement {
	
	protected int loadPoint;
	protected int replication;
	
	public Measurement(int loadPoint, int replication) {
		super();
		this.loadPoint = loadPoint;
		this.replication = replication;
	}

	public int getLoadPoint() {
		return loadPoint;
	}

	public int getReplication() {
		return replication;
	}
	
	
	
	

}

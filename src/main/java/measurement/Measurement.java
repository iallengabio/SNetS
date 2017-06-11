package measurement;

import java.io.IOException;
import java.util.List;

import request.RequestForConnection;
import simulationControl.resultManagers.IResultManager;

/**
 * This class represents the performance metrics used in the simulations.
 * 
 * @author Iallen
 */
public abstract class Measurement {
	
	protected int loadPoint;
	protected int replication;
	
	protected String fileName;
	protected IResultManager resultManager;
	
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
	 * Adds a new observation of performance metrics
	 * 
	 * @param success boolean
	 * @param request RequestForConnection
	 */
	public abstract void addNewObservation(boolean success, RequestForConnection request);

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
	
	/**
	 * To save the performance metric observations to a file
	 * 
	 * @param path String
	 * @param llm List<List<Measurement>>
	 * @throws IOException
	 */
	public void result(String path, List<List<Measurement>> llm) throws IOException{
		resultManager.result(path, fileName, llm);
	}
	
}

package measurement;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import network.ControlPlane;
import request.RequestForConnection;
import simulationControl.resultManagers.ResultManagerInterface;

/**
 * This class represents the performance metrics used in the simulations.
 * 
 * @author Iallen
 */
public abstract class Measurement implements Serializable {
	
	protected int loadPoint;
	protected int replication;

	protected ResultManagerInterface resultManager;
	
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
	 * @param cp ControlPlane
	 * @param success boolean
	 * @param request RequestForConnection
	 */
	public abstract void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request);

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
	 * Returns the file name
	 * 
	 * @return String
	 */
	public abstract String getFileName();
	
	/**
	 * This method returns a formatted string with the results generated by performance metrics.
	 *
	 * @param llm List<List<Measurement>>
	 */
	public String result(List<List<Measurement>> llm){
		return resultManager.result(llm);
	}
	
}

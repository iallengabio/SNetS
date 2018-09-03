package request;

import network.Circuit;
import network.Pair;
import network.RequestGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the requests made by the clients for the establishment of 
 * connections between two nodes of the network
 * 
 * @author Iallen
 */
public class RequestForConnection implements Serializable {
	
	protected Pair pair;
	protected double timeOfRequestHours;
	protected double timeOfFinalizeHours;
	protected double requiredBandwidth;
	protected RequestGenerator rg;
	
	protected List<Circuit> circuit = new ArrayList<>(); // Circuit that attends this request
	
	/**
	 * Returns the pair
	 * 
	 * @return Pair
	 */
	public Pair getPair() {
		return pair;
	}

	/**
	 * Sets the pair
	 * 
	 * @param pair Pair
	 */
	public void setPair(Pair pair) {
		this.pair = pair;
	}

	/**
	 * Returns request start time
	 * 
	 * @return double
	 */
	public double getTimeOfRequestHours() {
		return timeOfRequestHours;
	}

	/**
	 * Sets the request start time
	 * 
	 * @param timeOfRequestHours double
	 */
	public void setTimeOfRequestHours(double timeOfRequestHours) {
		this.timeOfRequestHours = timeOfRequestHours;
	}

	/**
	 * Returns request end time
	 * 
	 * @return double
	 */
	public double getTimeOfFinalizeHours() {
		return timeOfFinalizeHours;
	}

	/**
	 * Sets the request end time
	 * 
	 * @param timeOfFinalizeHours double
	 */
	public void setTimeOfFinalizeHours(double timeOfFinalizeHours) {
		this.timeOfFinalizeHours = timeOfFinalizeHours;
	}

	/**
	 * Returns the request bandwidth
	 * 
	 * @return double
	 */
	public double getRequiredBandwidth() {
		return requiredBandwidth;
	}

	/**
	 * Sets the request bandwidth
	 * 
	 * @param requiredBandwidth double
	 */
	public void setRequiredBandwidth(double requiredBandwidth) {
		this.requiredBandwidth = requiredBandwidth;
	}

	/**
	 * Returns the circuit that attends this request
	 * 
	 * @return Circuit
	 */
	public List<Circuit> getCircuits() {
		return circuit;
	}

	/**
	 * Sets the circuit that attends this request
	 * 
	 * @param circuits Circuit
	 */
	public void setCircuit(List<Circuit> circuits) {
		this.circuit = circuits;
	}
	/**
	 * Returns the request generator
	 * 
	 * @return RequestGenerator
	 */
	public RequestGenerator getRequestGenerator() {
		return rg;
	}

	/**
	 * Sets the request generator
	 * 
	 * @param rg RequestGenerator
	 */
	public void setRequestGenerator(RequestGenerator rg) {
		this.rg = rg;
	}
}

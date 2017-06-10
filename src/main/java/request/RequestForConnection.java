package request;

import network.Circuit;
import network.Link;
import network.Pair;
import network.RequestGenerator;

/**
 * This class represents the requests made by the clients for the establishment of 
 * connections between two nodes of the network
 * 
 * @author Iallen
 */
public class RequestForConnection {
	
	protected Pair pair;
	protected double timeOfRequestHours;
	protected double timeOfFinalizeHours;
	protected double requiredBandwidth;
	protected RequestGenerator rg;
	
	protected Circuit circuit; // Circuit that attends this request
	
	
	protected double SNR; //dB
	protected boolean QoT;
	protected boolean QoTForOther;

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
	public Circuit getCircuit() {
		return circuit;
	}

	/**
	 * Sets the circuit that attends this request
	 * 
	 * @param circuit Circuit
	 */
	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;		
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

	/**
	 * @return the sNR
	 */
	public double getSNR() {
		return SNR;
	}

	/**
	 * @param sNR the sNR to set
	 */
	public void setSNR(double sNR) {
		SNR = sNR;
	}

	/**
	 * @return the qoT
	 */
	public boolean isQoT() {
		return QoT;
	}

	/**
	 * @param qoT the qoT to set
	 */
	public void setQoT(boolean qoT) {
		QoT = qoT;
	}

	/**
	 * @return the qoTForOther
	 */
	public boolean isQoTForOther() {
		return QoTForOther;
	}

	/**
	 * @param qoTForOther the qoTForOther to set
	 */
	public void setQoTForOther(boolean qoTForOther) {
		QoTForOther = qoTForOther;
	}

	/**
	 * Este metodo retorna o espectro alocado pela requisicao em um enlace
	 * Pode mudar de acordo com o tipo de requisicao
	 * @param link - Link
	 * @return int[]
	 */
	public int[] getSpectrumAssignedByLink(Link link){
		int sa[] = circuit.getSpectrumAssigned();
		return sa;
	}
}

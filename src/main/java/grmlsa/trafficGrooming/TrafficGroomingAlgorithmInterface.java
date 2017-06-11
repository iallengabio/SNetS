package grmlsa.trafficGrooming;

import network.controlPlane.TransparentControlPlane;
import request.RequestForConnection;

/**
 * This interface should be implemented by classes of traffic grooming algorithms.
 * 
 * @author Iallen
 */
public interface TrafficGroomingAlgorithmInterface {
	
	/**
	 * This method defines which circuits will be used to aggregate traffic.
	 * Even in the absence of circuits to perform the traffic grooming, the algorithms may require the creation of new circuits to meet the new request.
	 * 
	 * @param rfc RequestForConnection
	 * @return boolean if the new request could be met.
	 */
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, TransparentControlPlane cp);
	
	/**
	 * Defines what should be done at the end of a connection.
	 * For example: finish the circuit; keep the circuit active; reduce the number of slots allocated for the circuit, etc.
	 * 
	 * @param rfc RequestForConnection
	 * @param cp ControlPlane
	 */
	public void finishConnection(RequestForConnection rfc, TransparentControlPlane cp);
}

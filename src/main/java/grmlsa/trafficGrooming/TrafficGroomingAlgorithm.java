package grmlsa.trafficGrooming;

import grmlsa.GRMLSA;
import network.ControlPlane;
import request.RequestForConnection;

public interface TrafficGroomingAlgorithm {
	
	/**
	 * This method defines which circuits will be used to aggregate traffic.
	 * Even in the absence of circuits to perform the traffic grooming, the algorithms may require the creation of new circuits to meet the new request.
	 * @param rfc
	 * @return if the new request could be met.
	 */
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane grmlsa);
	
	/**
	 * Defines what should be done at the end of a connection.
	 * For example: finish the circuit; keep the circuit active; reduce the number of slots allocated for the circuit, etc.
	 * @param rfc
	 * @param grmlsa
	 */
	public void finishConnection(RequestForConnection rfc, ControlPlane grmlsa);
}

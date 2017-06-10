package grmlsa.trafficGrooming;

import grmlsa.GRMLSA;
import network.Circuit;
import request.RequestForConnection;

/**
 * This class represents a technique that does not perform traffic grooming.
 * For each request, the control plane will try to create a new circuit.
 *
 * @author Iallen
 */
public class NoTrafficGrooming implements TrafficGroomingAlgorithm {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, GRMLSA grmlsa) {

		Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return grmlsa.getControlPlane().establishCircuit(circuit);
	}

	@Override
	public void finishConnection(RequestForConnection rfc, GRMLSA grmlsa) {
		grmlsa.getControlPlane().releaseCircuit(rfc.getCircuit());
	}
	
	

}

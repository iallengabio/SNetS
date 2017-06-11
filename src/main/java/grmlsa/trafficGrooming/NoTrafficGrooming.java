package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

/**
 * This class represents a technique that does not perform traffic grooming.
 * For each request, the control plane will try to create a new circuit.
 *
 * @author Iallen
 */
public class NoTrafficGrooming implements TrafficGroomingAlgorithmInterface {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) {

		Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return cp.establishCircuit(circuit);
	}

	@Override
	public void finishConnection(RequestForConnection rfc, ControlPlane cp) {
		cp.releaseCircuit(rfc.getCircuit());
	}

}

package grmlsa.trafficGrooming;

import grmlsa.GRMLSA;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConexion;

/**
 * Este algoritmo n�o faz agrega��o de trafego, ele simplesmente aloca um circuito para cada requisi��o de conex�o.
 * @author Iallen
 *
 */
public class NoTrafficGrooming implements TrafficGroomingAlgorithm {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConexion rfc, GRMLSA grmlsa) {
		
		//simplesmente tentar alocar um circuito para esta requisi��o!!!
		Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return grmlsa.getControlPlane().allocarCircuito(circuit);
	}

	@Override
	public void finalizarConexao(RequestForConexion rfc, GRMLSA grmlsa) {
		grmlsa.getControlPlane().desalocarCircuito(rfc.getCircuit());
	}
	
	

}

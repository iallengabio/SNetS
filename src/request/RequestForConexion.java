package request;

import network.Circuit;
import network.Pair;
import network.RequestGenerator;

/*
 * Esta classe representa as requisições feitas pelos clientes para o estabelecimento de conexões entre dois nós da rede
 */
public class RequestForConexion {
	
	protected Pair pair;
	protected double timeOfRequestHours;
	protected double timeOfFinalizeHours;
	protected double requiredBandwidth;
	protected RequestGenerator rg;
	
	protected Circuit circuit; //circuito que atende à esta requisição

	public Pair getPair() {
		return pair;
	}

	public void setPair(Pair pair) {
		this.pair = pair;
	}

	public double getTimeOfRequestHours() {
		return timeOfRequestHours;
	}

	public void setTimeOfRequestHours(double timeOfRequestHours) {
		this.timeOfRequestHours = timeOfRequestHours;
	}

	public double getTimeOfFinalizeHours() {
		return timeOfFinalizeHours;
	}

	public void setTimeOfFinalizeHours(double timeOfFinalizeHours) {
		this.timeOfFinalizeHours = timeOfFinalizeHours;
	}

	public double getRequiredBandwidth() {
		return requiredBandwidth;
	}

	public void setRequiredBandwidth(double requiredBandwidth) {
		this.requiredBandwidth = requiredBandwidth;
	}

	public Circuit getCircuit() {
		return circuit;
	}

	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;		
	}

	public RequestGenerator getRequestGenerator() {
		return rg;
	}

	public void setRequestGenerator(RequestGenerator rg) {
		this.rg = rg;
	}
	
	
	
	
	
	

}

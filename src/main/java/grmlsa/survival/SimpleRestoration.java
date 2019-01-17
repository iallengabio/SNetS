package grmlsa.survival;

import network.Circuit;
import network.SurvivalControlPlane;
import request.RequestForConnection;

/**
 * This class represents the simple restoration
 * 
 * @author Alexandre
 */
public class SimpleRestoration implements SurvivalStrategyInterface {

	@Override
	public boolean applyStrategy(RequestForConnection rfc, SurvivalControlPlane cp) throws Exception {
		// Applies the traffic aggregation algorithm
		return cp.getGrooming().searchCircuitsForGrooming(rfc, cp);
	}
	
	@Override
	public boolean applyStrategy(Circuit circuit, SurvivalControlPlane cp) {
		
		return false;
	}

	@Override
	public boolean thereAreFreeTransponders(Circuit circuit){
    	return (circuit.getSource().getTxs().hasFreeTransmitters() && circuit.getDestination().getRxs().hasFreeRecivers());
    }
	
	@Override
	public boolean survive(Circuit circuit){
		
		// libera os recursos da rota que falhou
		
		// busca uma rota dinamica sem falha
		
		// verifica a sobreviv�ncia utilizando a nova rota
		
		// se n�o sobreviveu remove da maquina de eventos
		
		
		return true;
	}

}

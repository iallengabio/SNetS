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
	public boolean survive(Circuit circuit){
		
		// libera os recursos da rota que falhou
		
		// busca uma rota dinamica sem falha
		
		// verifica a sobrevivência utilizando a nova rota
		
		// se não sobreviveu remove da maquina de eventos
		
		
		return true;
	}

}

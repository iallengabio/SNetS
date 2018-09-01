package grmlsa.survival;

import network.Circuit;
import network.SurvivalControlPlane;
import request.RequestForConnection;

/**
 * this class represents the dedicate path protection
 * 
 * @author Alexandre
 */
public class DedicatedPathProtection implements SurvivalStrategyInterface {

	
	@Override
	public boolean applyStrategy(RequestForConnection rfc, SurvivalControlPlane cp) throws Exception {
		
		// Applies the traffic aggregation algorithm
		cp.getGrooming().searchCircuitsForGrooming(rfc, cp);
		
		return true;
	}
	
	
	@Override
	public boolean survive(Circuit circuit) throws Exception {
		
		
		
		
		
		
		
		return true;
	}

	
}

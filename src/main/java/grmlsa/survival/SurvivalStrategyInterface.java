package grmlsa.survival;

import network.Circuit;
import network.SurvivalCircuit;
import network.SurvivalControlPlane;
import request.RequestForConnection;


/**
 * This interface must be implemented by the classes that represent the survival strategy.
 * 
 * @author Alexandre
 */
public interface SurvivalStrategyInterface {
	
	/**
	 * Applies the survival strategy
	 * 
	 * @param rfc RequestForConnection
	 * @param cp SurvivalControlPlane
	 * @return boolean
	 * @throws Exception
	 */
	public boolean applyStrategy(RequestForConnection rfc, SurvivalControlPlane cp) throws Exception;
	
	/**
	 * Applies the survival strategy
	 * 
	 * @param circuit SurvivalCircuit
	 * @param cp SurvivalControlPlane
	 * @return boolean
	 * @throws Exception
	 */
	public boolean applyStrategy(Circuit circuit, SurvivalControlPlane cp);
	
	/**
	 * Returns true if it can survive the fault and false otherwise.
	 * 
	 * @param circuit Circuit
	 * @return boolean
	 * @throws Exception
	 */
	public boolean survive(Circuit circuit) throws Exception;
	
	/**
	 * Verifies if there are free transmitters and receivers for the establishment of the new circuit
	 * 
	 * @param circuit Circuit
	 * @return boolean
	 */
	public boolean thereAreFreeTransponders(Circuit circuit);

}

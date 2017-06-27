package grmlsa.survival;

import network.Circuit;


/**
 * This interface must be implemented by the classes that represent the protection techniques.
 * 
 * @author Alexandre
 */
public interface ProtectionAlgorithmInterface {
	
	
	/**
	 * Returns true if it can survive the fault and false otherwise.
	 * 
	 * @return boolean
	 */
	public boolean survive(Circuit circuit);

}

package grmlsa.regeneratorAssignment;

import network.TranslucentCircuit;
import network.TranslucentControlPlane;

import java.io.Serializable;

/**
 * This interface should be implemented by classes of regenerator assignment algorithms independent of the routing.
 * 
 * @author Alexandre
 */
public interface RegeneratorAssignmentAlgorithmInterface extends Serializable {
	
	/**
	 * This method assigns a number of regenerator and returns true.
	 * f it is not possible to do the assignment, it should return false.
	 * 
	 * @param circuit CircuitTranslucent
	 * @return boolean
	 */
	public boolean assignRegenerator(TranslucentCircuit circuit, TranslucentControlPlane controlPlane);
	
}

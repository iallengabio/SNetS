package grmlsa.regeneratorAssignment;

import network.TranslucentCircuit;
import network.TranslucentControlPlane;

/**
 * This interface should be implemented by classes of regenerator assignment algorithms independent of the routing.
 * 
 * @author Alexandre
 */
public interface RegeneratorAssignmentAlgorithmInterface {
	
	/**
	 * This method assigns a number of regenerator and returns true.
	 * f it is not possible to do the assignment, it should return false.
	 * 
	 * @param circuit CircuitTranslucent
	 * @param spectrumAssignment SpectrumAssignmentAlgorithmInterface
	 * @return boolean
	 */
	public boolean assignRegenerator(TranslucentCircuit circuit, TranslucentControlPlane controlPlane);
	
}

package grmlsa.regeneratorAssignment;

import grmlsa.spectrumAssignment.SpectrumAssignmentInterface;
import network.CircuitTranslucent;

/**
 * This interface should be implemented by classes of regenerator assignment algorithms independent of the routing.
 * 
 * @author Alexandre
 */
public interface RegeneratorAssignmentIterface {
	
	/**
	 * This method assigns a number of regenerator and returns true.
	 * f it is not possible to do the assignment, it should return false.
	 * 
	 * @param circuit CircuitTranslucent
	 * @param spectrumAssignment SpectrumAssignmentIterface
	 * @return boolean
	 */
	public boolean assignRegenerator(CircuitTranslucent circuit, SpectrumAssignmentInterface spectrumAssignment);
	
}

package grmlsa.spectrumAssignment;

import network.Circuit;


/**
 * This interface should be implemented by classes of spectrum assignment algorithms independent of the routing.
 * 
 * @author Iallen
 */
public interface SpectrumAssignmentAlgorithmInterface {
	
	/**
	 * This method assigns a range of spectrum and returns true.
     * If it is not possible to do the assignment, it should return false.
	 *
	 * @param numberOfSlots int
	 * @param circuit Circuit
	 * @return boolean
	 */
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit);

}

package grmlsa.spectrumAssignment;

import java.io.Serializable;
import java.util.List;

import network.Circuit;
import network.ControlPlane;


/**
 * This interface should be implemented by classes of spectrum assignment algorithms independent of the routing.
 * 
 * @author Iallen
 */
public interface SpectrumAssignmentAlgorithmInterface extends Serializable {
	
	/**
	 * This method assigns a range of spectrum and returns true.
     * If it is not possible to do the assignment, it should return false.
	 *
	 * @param numberOfSlots int
	 * @param circuit Circuit
	 * @param cp ControlPlane
	 * @return boolean
	 */
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp);
	
	/**
	 * This method applies the specific policy of the spectrum allocation algorithm
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @param Circuit circuit
	 * @param cp ControlPlane
	 * @return int[]
	 */
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp);

}
package grmlsa.modulation;

import java.io.Serializable;
import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

/**
 * This interface should be implemented by classes of modulation selection algorithms.
 * 
 * @author Alexandre
 */
public interface ModulationSelectionAlgorithmInterface extends Serializable {
	
	/**
	 * This method selects the appropriate modulation format for the establishment of the circuit.
	 * 
	 * @param circuit Circuit
	 * @param route Route
	 * @param spectrumAssignment SpectrumAssignmentAlgorithmInterface
	 * @param cp ControlPlane
	 * @return Modulation
	 */
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp);
	
}

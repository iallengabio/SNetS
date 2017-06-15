package grmlsa.modulation;

import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;

/**
 * This interface should be implemented by classes of modulation selection algorithms.
 * 
 * @author Alexandre
 */
public interface ModulationSelectionAlgorithmInterface {
	
	/**
	 * This method selects the appropriate modulation format for the establishment of the circuit.
	 * 
	 * @param circuit Circuit
	 * @param route Route
	 * @param spectrumAssignment SpectrumAssignmentAlgorithmInterface
	 * @param mesh Mesh
	 * @return Modulation
	 */
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, Mesh mesh);
	
	/**
	 * This method returns the available modulation formats.
	 * 
	 * @return List<Modulation>
	 */
	public List<Modulation> getAvaliableModulations();
	
	/**
	 * Configures the available modulation formats.
	 * 
	 * @param avaliableModulations List<Modulation>
	 */
	public void setAvaliableModulations(List<Modulation> avaliableModulations);
}
package grmlsa.modulation;

import java.util.ArrayList;
import java.util.List;

import network.Mesh;

/**
 * This class is responsible for managing the modulation formats.
 * 
 * @author Iallen
 */
public class ModulationSelector {
	
	/**
	 * Distances based on articles:
	 *  - Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks
	 *  - Evaluating Internal Blocking in Noncontentionless Flex-grid ROADMs
	 *  - On the Complexity of Routing and Spectrum Assignment in Flexible-Grid Ring Networks
	 * 
	 * Other values:
	 *  - Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats
	 *  - Physical Layer Transmitter and Routing Optimization to Maximize the Traffic Throughput of a Nonlinear Optical Mesh Network
	 *  - Quantifying the Impact of Non-linear Impairments on Blocking Load in Elastic Optical Networks
	 * 
	 * @param mesh Mesh
	 * @return List<Modulation
	 */
	public static List<Modulation> configureModulations(Mesh mesh){
		double freqSlot = mesh.getLinkList().get(0).getSlotSpectrumBand();
		double rateFEC = mesh.getPhysicalLayer().getRateOfFEC();
		int guardBand = mesh.getGuardBand();
		boolean activeQoT = mesh.getPhysicalLayer().isActiveQoT();
		
		List<Modulation> avaliableModulations = new ArrayList<>();
		// String name, double maxRange, double M, double SNRthreshold, double rateFEC, double freqSlot, int guardBand, boolean activeQoT
		avaliableModulations.add(new Modulation("BPSK", 10000.0, 2.0, 6.0, rateFEC, freqSlot, guardBand, activeQoT));
		avaliableModulations.add(new Modulation("QPSK", 5000.0, 4.0, 9.0, rateFEC, freqSlot, guardBand, activeQoT));
		avaliableModulations.add(new Modulation("8QAM", 2500.0, 8.0, 12.0, rateFEC, freqSlot, guardBand, activeQoT));
		avaliableModulations.add(new Modulation("16QAM", 1250.0, 16.0, 15.0, rateFEC, freqSlot, guardBand, activeQoT));
		avaliableModulations.add(new Modulation("32QAM", 625.0, 32.0, 18.0, rateFEC, freqSlot, guardBand, activeQoT));
		avaliableModulations.add(new Modulation("64QAM", 312.0, 64.0, 21.0, rateFEC, freqSlot, guardBand, activeQoT));
		
		return avaliableModulations;
	}
	
}

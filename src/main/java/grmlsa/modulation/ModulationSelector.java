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
	 * "Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks"
	 * "Evaluating Internal Blocking in Noncontentionless Flex-grid ROADMs"
	 * "On the Complexity of Routing and Spectrum Assignment in Flexible-Grid Ring Networks"
	 * 
	 * Other values:
	 * "Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats"
	 * 
	 * @param mesh Mesh
	 * @return List<Modulation
	 */
	public static List<Modulation> configureModulations(Mesh mesh){
		double freqSlot = mesh.getLinkList().get(0).getSlotSpectrumBand();
		int guardBand = mesh.getGuardBand();
		
		List<Modulation> avaliableModulations = new ArrayList<>();
		//String name, double bitsPerSymbol, double freqSlot, double maxRange, int guardBand, double level, double k2, double M
		avaliableModulations.add(new Modulation("BPSK", 1.75, freqSlot, 10000.0, guardBand, 2.0, 1.0, 2.0, mesh));
		avaliableModulations.add(new Modulation("QPSK", 3.33, freqSlot, 5000.0, guardBand, 3.0, 1.0, 4.0, mesh));
		avaliableModulations.add(new Modulation("8QAM", 4.50, freqSlot, 2500.0, guardBand, 4.0, 1.0, 8.0, mesh));
		avaliableModulations.add(new Modulation("16QAM", 6.67, freqSlot, 1250.0, guardBand, 5.0, 1.8, 16.0, mesh));
		avaliableModulations.add(new Modulation("32QAM", 13.32, freqSlot, 625.0, guardBand, 6.0, 1.7, 32.0, mesh));
		avaliableModulations.add(new Modulation("64QAM", 23.64, freqSlot, 312.0, guardBand, 7.0, 2.333, 64.0, mesh));
		
		return avaliableModulations;
	}
	
}

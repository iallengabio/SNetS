package grmlsa.modulation;

import java.util.ArrayList;
import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;

/**
 * This class is responsible for managing the modulation formats.
 * 
 * @author Iallen
 */
public class ModulationSelector {
	
	private List<Modulation> avaliableModulations;
	
	/**
	 * Distances based on articles:
	 * "Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks"
	 * "Evaluating Internal Blocking in Noncontentionless Flex-grid ROADMs"
	 * "On the Complexity of Routing and Spectrum Assignment in Flexible-Grid Ring Networks"
	 * 
	 * Other values:
	 * "Error Vector Magnitude as a Performance Measure for Advanced Modulation Formats"
	 * 
	 * @param freqSlot double
	 * @param guardBand int
	 * @param mesh Mesh
	 */
	public ModulationSelector(double freqSlot, int guardBand, Mesh mesh){
		avaliableModulations = new ArrayList<>();
		//String name, double bitsPerSymbol, double freqSlot, double maxRange, int guardBand, double level, double k2, double M
		avaliableModulations.add(new Modulation("BPSK", 1.75, freqSlot, 10000.0, guardBand, 2.0, 1.0, 2.0, mesh));
		avaliableModulations.add(new Modulation("QPSK", 3.33, freqSlot, 5000.0, guardBand, 3.0, 1.0, 4.0, mesh));
		avaliableModulations.add(new Modulation("8QAM", 4.50, freqSlot, 2500.0, guardBand, 4.0, 1.0, 8.0, mesh));
		avaliableModulations.add(new Modulation("16QAM", 6.67, freqSlot, 1250.0, guardBand, 5.0, 1.8, 16.0, mesh));
		avaliableModulations.add(new Modulation("32QAM", 13.32, freqSlot, 625.0, guardBand, 6.0, 1.7, 32.0, mesh));
		avaliableModulations.add(new Modulation("64QAM", 23.64, freqSlot, 312.0, guardBand, 7.0, 2.333, 64.0, mesh));
	}
	
	/**
	 * This method selects the process of selecting the modulation format
     * The modulation format can be selected by its range or QoT
     * 
	 * @param circuit - Circuit
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgorithmInterface
	 * @return Modulation
	 */
	public Modulation selectModulation (Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, Mesh mesh) {
		Modulation resMod = null;
		
		if(mesh.getPhysicalLayer().isActiveQoT()){
			resMod = selectModulationByQoT(circuit, route, spectrumAssignment, mesh);
		}else{
			resMod = selectModulationByDistance(circuit, route);
		}
		
		return resMod;
	}

	/**
	 * Returns modulation robust enough to satisfy the request with the highest bit rate per possible symbol
	 * 
	 * @param circuit - Circuit
	 * @param route - Route
	 * @return Modulation
	 */
	public Modulation selectModulationByDistance (Circuit circut, Route route){
		double maxBPS = 0.0;
		Modulation resMod = null;
		
		for (Modulation mod : avaliableModulations) {
			if(mod.getMaxRange() >= route.getDistanceAllLinks()){//Modulation robust enough for this requirement
				if(mod.getBitsPerSymbol() > maxBPS){ //Choose the modulation with the largest number of bits per possible symbol
					resMod = mod;
					maxBPS = mod.getBitsPerSymbol();
				}
			}
		}
		
		return resMod;
	}
	
	/**
	 * Returns enough robust modulation to satisfy the request with the highest bit rate per symbol possible 
	 * considering the quality of transmission
	 * 
	 * @param circuit - Circuit
	 * @param route - Route
	 * @param spectrumAssignment - SpectrumAssignmentAlgorithmInterface
	 * @param mesh - Mesh
	 * @return Modulation
	 */
	public Modulation selectModulationByQoT(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, Mesh mesh){
		Modulation resMod = null; //For admissible QoT
		Modulation alternativeMod = null; //Which at least allocates spectrum
		
		for(int i = 0; i < avaliableModulations.size(); i++){
			Modulation mod = avaliableModulations.get(i);
			
			if(spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit)){
				if(alternativeMod == null){
					alternativeMod = mod; //The first modulation that was able to allocate
				}
				
				boolean flagQoT = mesh.getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, circuit.getSpectrumAssigned());
				if(flagQoT){
					resMod = mod; //Save the modulation that has admissible QoT
				}
			}
		}
		
		if(resMod == null){ //QoT is not acceptable for all modulations
			if(alternativeMod != null){ //Allocated spectrum using some modulation, but the one that was inadmissible
				resMod = alternativeMod;
				circuit.setQoT(false); //To mark that the blockade was by QoT inadmissible
			}
		}
		
		return resMod;
	}
	
	/**
	 * This method returns the available modulation formats
	 * 
	 * @return the avaliableModulations - List<Modulation>
	 */
	public List<Modulation> getAvaliableModulations() {
		return avaliableModulations;
	}
	
}

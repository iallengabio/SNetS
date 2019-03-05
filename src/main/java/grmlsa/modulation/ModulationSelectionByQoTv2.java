package grmlsa.modulation;

import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

/**
 * This class implements the modulation selection algorithm by quality of transmission.
 * The spectrum allocation of each modulation is also checked.
 * The search for modulation starts from the most spectrally efficient modulation.
 * Information such as modulation and selected spectrum, and quality of transmission, are stored in the circuit.
 * 
 * @author Alexandre
 */
public class ModulationSelectionByQoTv2 implements ModulationSelectionAlgorithmInterface {
	
	private List<Modulation> avaliableModulations;

	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		if(avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}
		
		boolean flagQoT = false; // Assuming that the circuit QoT starts as not acceptable
		
		// Modulation and spectrum selected
		Modulation chosenMod = null;
		int chosenBand[] = null;
		
		// Modulation which at least allocates spectrum, used to avoid error in metrics
		Modulation alternativeMod = null;
		int alternativeBand[] = null;
		
		// Begins with the most spectrally efficient modulation format
		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
			Modulation mod = avaliableModulations.get(m);
			circuit.setModulation(mod);
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			
			if(spectrumAssignment.assignSpectrum(numberOfSlots, circuit, cp)){
				int band[] = circuit.getSpectrumAssigned();
				
				if(band != null){
					alternativeMod = mod; // The last modulation that was able to allocate spectrum
					alternativeBand = band;
				}
				
				if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band)){
					chosenMod = mod; // Save the modulation that has admissible QoT
					chosenBand = band;
					
					flagQoT = true;
					
					break; // Stop when a modulation reaches admissible QoT
				}
			}
		}
		
		if(chosenMod == null){ // QoT is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
			
			if(alternativeMod != null){ // Allocated spectrum using some modulation, but the QoT was inadmissible 
				chosenMod = alternativeMod;
				chosenBand = alternativeBand;
			}
		}
		
		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		circuit.setQoT(flagQoT);
		
		return chosenMod;
	}
	
	@Override
	public List<Modulation> getAvaliableModulations() {
		return avaliableModulations;
	}
	
}

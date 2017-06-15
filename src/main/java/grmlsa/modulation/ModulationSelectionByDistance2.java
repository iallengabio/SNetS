package grmlsa.modulation;

import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;

/**
 * This class implements the modulation selection algorithm by maximum range.
 * The spectrum allocation of each modulation is also checked.
 * Information such as modulation and selected spectrum, and quality of transmission, are stored in the circuit.
 * 
 * @author Alexandre
 */
public class ModulationSelectionByDistance2 implements ModulationSelectionAlgorithmInterface {
	
	private List<Modulation> avaliableModulations;

	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, Mesh mesh) {
		boolean flagQoT = false; // Assuming that the circuit QoT starts as not acceptable
		
		// Modulation and spectrum selected
		Modulation chosenMod = null;
		int chosenBand[] = null;
		
		// Modulation which at least allocates spectrum, used to avoid error in metrics
		Modulation alternativeMod = null;
		int alternativeBand[] = null;
		
		for (int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			
			if(spectrumAssignment.assignSpectrum(numberOfSlots, circuit)){
				int band[] = circuit.getSpectrumAssigned();
				
				if(alternativeMod == null){
					alternativeMod = mod; // The first modulation that was able to allocate spectrum
					alternativeBand = band;
				}
				
				if(mod.getMaxRange() >= route.getDistanceAllLinks()){
					chosenMod = mod; // Save the modulation that has enough reach to meet the circuit
					chosenBand = band;
					
					flagQoT = true;
				}
			}
		}
		
		if(chosenMod == null){ // reach is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
			
			if(alternativeMod != null){ // Allocated spectrum using some modulation, but the reach is not enough 
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
	
	@Override
	public void setAvaliableModulations(List<Modulation> avaliableModulations){
		this.avaliableModulations = avaliableModulations;
	}
}

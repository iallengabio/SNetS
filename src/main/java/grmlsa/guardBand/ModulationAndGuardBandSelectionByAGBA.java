package grmlsa.guardBand;

import java.util.List;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;

/**
 * This class implements the choice of modulation and guard band using AGBA algorithm.
 * 
 * The implementation of the AGBA algorithm is presented in the article: 
 * Adaptive Guard-Band Assignment with Adaptive Spectral Profile Equalizer 
 * to Improve Spectral Usage of Impairment-Aware Elastic Optical Network (2016)
 * 
 * In the AGBA the size of guard band is selected based on the hop count
 * of the chosen route.
 * 
 * @authors Takeshita Et al.
 */
public class ModulationAndGuardBandSelectionByAGBA implements ModulationSelectionAlgorithmInterface {
	
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
			
			if(route.getHops() <= 1){
				mod.setGuardBand(1);
            }else{
            	mod.setGuardBand(2);
            }

            Modulation modClone = null;
            try {
            	modClone = (Modulation) mod.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
            
			circuit.setModulation(modClone);
			int numberOfSlots = modClone.requiredSlots(circuit.getRequiredBandwidth());
			
			if(spectrumAssignment.assignSpectrum(numberOfSlots, circuit, cp)){
				int band[] = circuit.getSpectrumAssigned();
				
				if(band != null){
					alternativeMod = modClone; // The last modulation that was able to allocate spectrum
					alternativeBand = band;
				}
				
				if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, band, null, false)){
					chosenMod = modClone; // Save the modulation that has admissible QoT
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

}

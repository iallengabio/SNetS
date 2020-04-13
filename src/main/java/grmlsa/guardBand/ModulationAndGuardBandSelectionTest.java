package grmlsa.guardBand;

import java.util.List;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

public class ModulationAndGuardBandSelectionTest implements ModulationSelectionAlgorithmInterface {
	
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
		
		// Minimum and maximum values for guard band
		int minGB = 1;
		int maxGB = 8;
		
		//double maxDeltaSNR;
		double circuitDeltaSNR;
		double sigma = 0.25; //SNR margin over the SNR threshold
		
		// Begins with the most spectrally efficient modulation format
		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
			Modulation mod = avaliableModulations.get(m);
			
			//maxDeltaSNR = 0.0;
			
			//for (int gb = maxGB; gb >= minGB; gb--) {
			for (int gb = minGB; gb <= maxGB; gb++) {
				
	            Modulation modClone = null;
	            try {
	            	modClone = (Modulation) mod.clone();
	            	modClone.setGuardBand(gb);
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
						flagQoT = true;
						
						circuitDeltaSNR = circuit.getSNR() - modClone.getSNRthreshold();
						
						//if (circuitDeltaSNR > maxDeltaSNR) {
						if (circuitDeltaSNR >= sigma) {
							//maxDeltaSNR = circuitDeltaSNR;
							
							chosenMod = modClone; // Save the modulation that has admissible QoT
							chosenBand = band;
							
							break;
						}
					}
				}
			}
			
			if (flagQoT) {
				break; // Stop when a modulation reaches admissible QoT
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

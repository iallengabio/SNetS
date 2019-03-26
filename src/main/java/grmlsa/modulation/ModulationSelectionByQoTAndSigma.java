package grmlsa.modulation;

import java.util.List;
import java.util.Map;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;


/**
 * This class implements the modulation selection algorithm by quality of transmission and sigma value.
 * The spectrum allocation of each modulation is also checked.
 * Information such as modulation and selected spectrum, and quality of transmission, are stored in the circuit.
 * 
 * Was based on the modulation selection algorithm presented in:
 * - An Efficient IA-RMLSA Algorithm for Transparent Elastic Optical Networks (2017)
 * 
 * This algorithm uses the sigma parameter to choose modulation format.
 * The value of sigma must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "sigma":"0.5"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class ModulationSelectionByQoTAndSigma implements ModulationSelectionAlgorithmInterface {
	
	private List<Modulation> avaliableModulations;
	private Double sigma;
	
	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		if(avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}
		
		if(sigma == null){ // read the sigma value
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			sigma = Double.parseDouble((String)uv.get("sigma"));
		}
		
		boolean flagQoT = false; // Assuming that the circuit QoT starts as not acceptable
		
		// Modulation and spectrum selected which respect the sigma value
		Modulation chosenMod = null;
		int chosenBand[] = null;
		
		// Modulation which at least allocates spectrum, used to avoid error in metrics
		Modulation alternativeMod = null;
		int alternativeBand[] = null;
		
		// Modulation that allocates spectrum and have QoT acceptable
		Modulation alternativeMod2 = null;
		int alternativeBand2[] = null;
		
		double deltaSNR = 0.0;
		
		for (int m = 0; m < avaliableModulations.size(); m++) {
			Modulation mod = avaliableModulations.get(m);
			circuit.setModulation(mod);
			int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
			
			if(spectrumAssignment.assignSpectrum(numberOfSlots, circuit, cp)){
				int band[] = circuit.getSpectrumAssigned();
				
				if(alternativeMod == null){
					alternativeMod = mod; // The first modulation that was able to allocate spectrum
					alternativeBand = band;
				}
				
				if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null)){
					alternativeMod2 = mod; // Save the modulation that has admissible QoT
					alternativeBand2 = band;
					
					flagQoT = true;
					
					deltaSNR = circuit.getSNR() - mod.getSNRthreshold();
					
					if(deltaSNR >= sigma){
						chosenMod = mod; // Save the modulation that has admissible QoT and respect the sigma value
						chosenBand = band;
					}
				}
			}
		}
		
		if(chosenMod == null){ // No modulation respects the sigma value
			chosenMod = alternativeMod2;
			chosenBand = alternativeBand2;
			
			if(alternativeMod2 == null){ // QoT is not enough for all modulations
				
				chosenMod = avaliableModulations.get(0); // To avoid metric error
				chosenBand = null;
				
				if(alternativeMod != null){ // Allocated spectrum using some modulation, but the QoT was inadmissible 
					chosenMod = alternativeMod;
					chosenBand = alternativeBand;
				}
			}
		}
		
		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		circuit.setQoT(flagQoT);
		
		return chosenMod;
	}
	
}

package grmlsa.guardBand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;

/**
 * This class implements the choice of modulation and guard band using GBUN algorithm.
 * 
 * The implementation of the GBUN algorithm presented in the article: 
 * - Novo algoritmo para Provisão de Banda de Guarda Adaptativa em Redes
 * Ópticas Elásticas (2019)
 * 
 * In the GBUN the size of guard band is selected based on the level
 * of the network utilization.
 * 
 * The utilizations values must be entered in the configuration file "others" as shown below according to the network.
 * {"variables":{
 *               "1":"0.266",
                 "2":"0.228",
                 "3":"0.19",
                 "4":"0.152",
                 "5":"0.114",
                 "6":"0.076",
                 "7":"0.038"
 *               }
 * }
 * 
 * @author Neclyeux
 */
public class ModulationAndGuardBandSelectionByGbun implements ModulationSelectionAlgorithmInterface {
	
	private List<Modulation> avaliableModulations;
	private List<Double> utilizations;

	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		
		if(avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}
		
		if(utilizations == null) {
			utilizations = new ArrayList<Double>();
			Map<String, String> utilizations  = cp.getMesh().getOthersConfig().getVariables();
			for (int i = 1; i <= utilizations.size(); i++) {
				double n = Double.parseDouble((String)utilizations.get(Integer.toString(i)));
				this.utilizations.add(n);
			}
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
			
			//Choose the guard band according to the utilization of the topology
			if(UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(0))
        		mod.setGuardBand(1);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(0) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(1))
            	mod.setGuardBand(2);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(1) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(2))
            	mod.setGuardBand(3);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(2) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(3))
            	mod.setGuardBand(4);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(3) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(4))
            	mod.setGuardBand(5);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(4) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(5))
            	mod.setGuardBand(6);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(5) && UtilizationGeneral(cp.getMesh()) >= this.utilizations.get(6))
            	mod.setGuardBand(7);
            if(UtilizationGeneral(cp.getMesh()) < this.utilizations.get(6))
            	mod.setGuardBand(8);

			
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
	
	/**
     * Returns the total usage of the topology 
     * 
     * @param mesh
     */
    private double UtilizationGeneral(Mesh mesh) {
        Double utGeneral = 0.0;
        for (Link link : mesh.getLinkList()) {
        	utGeneral += link.getUtilization();
        }

        utGeneral = utGeneral / (double) mesh.getLinkList().size();

        return utGeneral;
    }

}

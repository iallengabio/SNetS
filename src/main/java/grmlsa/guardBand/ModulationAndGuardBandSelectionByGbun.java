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
 * This class implements the choice of modulation and guard band using GBUN algorithm.
 * 
 * The implementation of the GBUN algorithm presented in the article: 
 * - Novo algoritmo para Provisão de Banda de Guarda Adaptativa em Redes
 * Ópticas Elásticas (2019)
 * 
 * In the GBUN the size of guard band is selected based on the level
 * of the network utilization.
 * 
 * @authors Neclyeux, Alexandre, Iallen, Antonio costa, Divanilson Campelo, André Soares
 */
public class ModulationAndGuardBandSelectionByGbun implements ModulationSelectionAlgorithmInterface {
	
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
			
			//NSFNet
			if(UtilizacaoGeral(cp.getMesh()) >= 0.266)
        		mod.setGuardBand(1);
            if(UtilizacaoGeral(cp.getMesh()) < 0.266 && UtilizacaoGeral(cp.getMesh()) >= 0.228)
            	mod.setGuardBand(2);
            if(UtilizacaoGeral(cp.getMesh()) < 0.228 && UtilizacaoGeral(cp.getMesh()) >= 0.19)
            	mod.setGuardBand(3);
            if(UtilizacaoGeral(cp.getMesh()) < 0.19 && UtilizacaoGeral(cp.getMesh()) >= 0.152)
            	mod.setGuardBand(4);
            if(UtilizacaoGeral(cp.getMesh()) < 0.152 && UtilizacaoGeral(cp.getMesh()) >= 0.114)
            	mod.setGuardBand(5);
            if(UtilizacaoGeral(cp.getMesh()) < 0.114 && UtilizacaoGeral(cp.getMesh()) >= 0.076)
            	mod.setGuardBand(6);
            if(UtilizacaoGeral(cp.getMesh()) < 0.076 && UtilizacaoGeral(cp.getMesh()) >= 0.038)
            	mod.setGuardBand(7);
            if(UtilizacaoGeral(cp.getMesh()) < 0.038)
            	mod.setGuardBand(8);

            /*//Cost239
            if(UtilizacaoGeral(cp.getMesh()) >= 0.245)
        		mod.setGuardBand(1);
            if(UtilizacaoGeral(cp.getMesh()) < 0.245 && UtilizacaoGeral(cp.getMesh()) >= 0.21)
            	mod.setGuardBand(2);
            if(UtilizacaoGeral(cp.getMesh()) < 0.21 && UtilizacaoGeral(cp.getMesh()) >= 0.175)
            	mod.setGuardBand(3);
            if(UtilizacaoGeral(cp.getMesh()) < 0.175 && UtilizacaoGeral(cp.getMesh()) >= 0.14)
            	mod.setGuardBand(4);
            if(UtilizacaoGeral(cp.getMesh()) < 0.14 && UtilizacaoGeral(cp.getMesh()) >= 0.105)
            	mod.setGuardBand(5);
            if(UtilizacaoGeral(cp.getMesh()) < 0.105 && UtilizacaoGeral(cp.getMesh()) >= 0.07)
            	mod.setGuardBand(6);
            if(UtilizacaoGeral(cp.getMesh()) < 0.07 && UtilizacaoGeral(cp.getMesh()) >= 0.035)
            	mod.setGuardBand(7);
            if(UtilizacaoGeral(cp.getMesh()) < 0.035)
            	mod.setGuardBand(8);*/
			
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
    private double UtilizacaoGeral(Mesh mesh) {
        Double utGeral = 0.0;
        for (Link link : mesh.getLinkList()) {
            utGeral += link.getUtilization();
        }

        utGeral = utGeral / (double) mesh.getLinkList().size();

        return utGeral;
    }

}

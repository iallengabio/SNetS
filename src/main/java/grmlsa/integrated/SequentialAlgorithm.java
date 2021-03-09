package grmlsa.integrated;

import java.util.List;
import java.util.TreeSet;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

/**
 * Algorithm created to apply sequentially the routing, modulation and spectrum allocation algorithms.
 * 
 * @author Alexandre
 *
 */
public class SequentialAlgorithm implements IntegratedRMLSAAlgorithmInterface {
	
    private RoutingAlgorithmInterface routing;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
    	
        if (routing == null){
        	routing = cp.getRouting();
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }
        
        routing.findRoute(circuit, cp.getMesh());
        Route route = circuit.getRoute();
        
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        
        Modulation chosenMod = null;
        int chosenBand[] = null;
        
        // to avoid metrics error
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		
    	// Begins with the most spectrally efficient modulation format
		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
			Modulation mod = avaliableModulations.get(m);
			circuit.setModulation(mod);
        	
        	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
            
            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
            circuit.setSpectrumAssigned(band);
            
            if (band != null) {
        		checkMod = mod;
        		checkBand = band;
            	
            	if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null, false)){ //modulation has acceptable QoT
            		chosenMod = mod;
            		chosenBand = band;
	                
	                break; // Stop when a modulation reaches admissible QoT
            	}
            }
        }
		
		boolean flagQoT = true; // Assuming that the QOT is acceptable 
		
		if(chosenMod == null){ // QoT is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
			
			if(checkMod != null){ // Allocated spectrum using some modulation, but the QoT was inadmissible 
				chosenMod = checkMod;
				chosenBand = checkBand;
			}
			
			flagQoT = false;
		}
		
		circuit.setRoute(route);
        circuit.setModulation(chosenMod);
        circuit.setSpectrumAssigned(chosenBand);

        return flagQoT;
    }
    
    /**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return null;
    }
}
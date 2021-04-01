package grmlsa.integrated;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import grmlsa.*;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

/**
 * RMLSA integrated algorithm that reads the spectrum assignment of the simulation configuration file
 * Searches the modulation format from the most spectrally efficient to the least efficient
 * 
 * @author Alexandre
 *
 */
public class KShortestPathsAndSpectrumAssignment_v2 implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			String krt = (String)uv.get("krtype");
			if(uv.get("k")!=null)
				k = Integer.parseInt((String)uv.get("k"));

			switch(krt){
				case "ksp":
				case "kspd":
					kShortestsPaths = new KSPDistance(cp.getMesh(), k);
					break;
				case "ksph":
					kShortestsPaths = new KSPHops(cp.getMesh(), k);
					break;
				case "kgp":
					kShortestsPaths = new KGP(cp, k);
					break;
				default:
					kShortestsPaths = new KSPDistance(cp.getMesh(), k);
			}
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = null;
        
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        
        // to avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		
  		int minNumCircuits = Integer.MAX_VALUE;
  		
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
            int numCircuits = computeNumCircuitsByRoute(route);
            
            if(numCircuits < minNumCircuits){
            	minNumCircuits = numCircuits;
            	
            	// Begins with the most spectrally efficient modulation format
        		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
        			Modulation mod = avaliableModulations.get(m);
        			circuit.setModulation(mod);
	            	
	            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
		            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
		            
		            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
		            circuit.setSpectrumAssigned(band);
		
		            if (band != null) {
	            		checkRoute = route;
	            		checkMod = mod;
	            		checkBand = band;
		            	
		            	if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null, false)){ //modulation has acceptable QoT
		            		chosenRoute = route;
		            		chosenBand = band;
			                chosenMod = mod;
			                
			                break; // Stop when a modulation reaches admissible QoT
		            	}
		            }
	            }
            }
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);

            return true;

        } else {
        	if(checkRoute == null){
				checkRoute = candidateRoutes.get(0);
				checkMod = avaliableModulations.get(0);
			}
            circuit.setRoute(checkRoute);
            circuit.setModulation(checkMod);
            circuit.setSpectrumAssigned(checkBand);

            return false;
        }
    }
    
    /**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return kShortestsPaths;
    }
    
    private int computeNumCircuitsByRoute(Route route){
    	HashSet<Circuit> circuits = new HashSet<Circuit>();
    	
    	for(Link link : route.getLinkList()){
    		
    		HashSet<Circuit> circuitsTemp = link.getCircuitList();
    		for(Circuit circuitTemp : circuitsTemp){
    			
    			if(!circuits.contains(circuitTemp)){
    				circuits.add(circuitTemp);
    			}
    		}
    	}
    	
    	return circuits.size();
    }
}

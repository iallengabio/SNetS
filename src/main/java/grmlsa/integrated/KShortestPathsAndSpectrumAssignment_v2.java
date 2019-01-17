package grmlsa.integrated;

import java.util.List;
import java.util.TreeSet;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.Route;
import grmlsa.YenKShortestPath;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

public class KShortestPathsAndSpectrumAssignment_v2 implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new YenKShortestPath(cp.getMesh().getNodeList(), cp.getMesh().getLinkList(), k, 1);
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
        int chosenBand[] = {999999, 999999}; // Value never reached
        
        List<Modulation> avaliableModulations = modulationSelection.getAvaliableModulations();

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
            
	            for(Modulation mod : avaliableModulations){
	            	circuit.setModulation(mod);
	            	
	            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
		            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
		            
		            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
		            circuit.setSpectrumAssigned(band);
		
		            if (band != null) {
	            		checkRoute = route;
	            		checkMod = mod;
	            		checkBand = band;
		            	
		            	if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band)){ //modulation has acceptable QoT
		            		chosenRoute = route;
		            		chosenBand = band;
			                chosenMod = mod;
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
    	TreeSet<Circuit> circuits = new TreeSet<Circuit>();
    	
    	for(Link link : route.getLinkList()){
    		
    		TreeSet<Circuit> circuitsTemp = link.getCircuitList();
    		for(Circuit circuitTemp : circuitsTemp){
    			
    			if(!circuits.contains(circuitTemp)){
    				circuits.add(circuitTemp);
    			}
    		}
    	}
    	
    	return circuits.size();
    }
}

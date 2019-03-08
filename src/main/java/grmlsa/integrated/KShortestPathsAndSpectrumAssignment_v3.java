package grmlsa.integrated;

import java.util.ArrayList;
import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

public class KShortestPathsAndSpectrumAssignment_v3 implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
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
        int kFF = 3;
        
        // to avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
        	// Begins with the most spectrally efficient modulation format
    		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
    			Modulation mod = avaliableModulations.get(m);
    			circuit.setModulation(mod);
            	
            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
	            
	            // for the k-FirstFit spectrum allocation algorithm
	            ArrayList<int[]> bandList = new ArrayList<int[]>();
	    		for (int[] band : merge) { // checks and guard the free bands that can establish the requisition
	    			if(band[1] - band[0] + 1 >= slotsNumber){
	    				
	    				int faixaTemp[] = band.clone();
	    				bandList.add(faixaTemp);
	    				
	    				if(bandList.size() == kFF) { // stop when you reach the k value of free bands
	    					break;
	    				}
	    			}
	    		}
	            
	    		// traverses the free spectrum bands
	    		for (int[] band : bandList) {
	    			checkBand = band.clone();
	    			checkBand[1] = checkBand[0] + slotsNumber - 1;
	    			
            		checkMod = mod;
            		checkBand = band;
	    			
	    			if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null)){ //modulation has acceptable QoT
	            		chosenRoute = route;
	            		chosenBand = band;
		                chosenMod = mod;
		                
		                break; // Stop when a modulation reaches admissible QoT
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
    
}

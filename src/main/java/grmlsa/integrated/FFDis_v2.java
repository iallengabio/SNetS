package grmlsa.integrated;

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

/**
 * This class implements the integrated RMLSA algorithm FF-Dis presented in the article:
 *  - SBRC 2020 - Novo RMLSA com Tonificação de Circuito e ciente da Qualidade de Transmissão com Baixa Margem em Redes Ópticas Elásticas
 * 
 * Note: The FF-Dis algorithm uses the distance modulation selection algorithm
 */
public class FFDis_v2 implements IntegratedRMLSAAlgorithmInterface {

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
    			
    			if(mod.getMaxRange() >= route.getDistanceAllLinks()){ // Modulation range is greater than or equal to the distance of the route
    				
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
    		if (chosenBand != null) {
    			break;
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
				checkMod = cp.getMesh().getAvaliableModulations().get(0);
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
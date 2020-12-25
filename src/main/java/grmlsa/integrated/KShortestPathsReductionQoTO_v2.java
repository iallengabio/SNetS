package grmlsa.integrated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

/**
 * Implementation based on the K Shortest Paths Reduction QoTO (KSP-RQoTO) algorithm presented in:
 *  - An Efficient IA-RMLSA Algorithm for Transparent Elastic Optical Networks (2017)
 * 
 * KSP-RQoTO algorithm uses the sigma parameter to choose modulation format.
 * The value of sigma must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "sigma":"0.5"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class KShortestPathsReductionQoTO_v2 implements IntegratedRMLSAAlgorithmInterface {
	
    private static int k = 3; // Number of candidate routes
	private NewKShortestPaths kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    // A single sigma value for all pairs
    private double sigma;
    
    // A sigma value for each pair
	private HashMap<String, HashMap<Route, Double>> sigmaForAllPairs;
	
	// Used as a separator between the names of nodes
    private static final String DIV = "-";
    
	@Override
	public boolean rsa(Circuit circuit, ControlPlane cp) {
		if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
        }
		if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection(); // Uses the modulation selection algorithm defined in the simulation file
        	
        	//read the sigma value
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			sigma = Double.parseDouble((String)uv.get("sigma"));
        }
        if(spectrumAssignment == null){
			spectrumAssignment = cp.getSpectrumAssignment(); // Uses the spectrum assignment algorithm defined in the simulation file
		}

        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached
        
        double chosenWorstDeltaSNR = 0.0;
        String pair = circuit.getPair().getSource().getName() + DIV + circuit.getPair().getDestination().getName();
		double sigmaPair = sigma;
		
		// To avoid metrics error
		Route checkRoute = null;
		Modulation checkMod = null;
		int checkBand[] = null;
        
		for (int r = 0; r < candidateRoutes.size(); r++) {
			Route routeTemp = candidateRoutes.get(r);
			circuit.setRoute(routeTemp);
			
			if(sigmaForAllPairs != null){
				sigmaPair = sigmaForAllPairs.get(pair).get(routeTemp);
			}
			
	    	Modulation firstModulation = null; // First modulation format option
	    	Modulation secondModulation = null; // Second modulation format option
	    	
	    	double firstHighestLevel = 0.0; // Level for the first modulation option
	    	double secondHighestLevel = 0.0; // Level for the second modulation option
	    	
	    	int firstBand[] = null; // Band of available slots found with the first modulation option
	    	int secondBand[] = null; // Band of available slots found with the second modulation option
	    	
			for(int m = 0; m < avaliableModulations.size(); m++){
				Modulation mod = avaliableModulations.get(m);
				circuit.setModulation(mod);
				
				// Number of slots required
				int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp, circuit.getGuardBand());
				
				// Applies the spectrum assignment algorithm that returns the band of available slots
				int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit, cp);
				circuit.setSpectrumAssigned(band);
				
				if(band != null){ // A band of available slots was found
					if(checkRoute == null){
						checkRoute = routeTemp;
						checkMod = mod;
						checkBand = band;
					}
					
					// Computes QoT for the new circuit
					boolean circuitQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, routeTemp, mod, band, null, false);
					
					if(circuitQoT){ // If QoT is acceptable
						double circuitDeltaSNR = circuit.getSNR() - mod.getSNRthreshold();
						
						if((circuitDeltaSNR >= sigmaPair) && (mod.getBitsPerSymbol() > firstHighestLevel)){ // Tries to choose the modulation format that respects the sigma value
							firstModulation = mod;
							firstBand = band;
							firstHighestLevel = mod.getBitsPerSymbol();
						}
						if(mod.getBitsPerSymbol() > secondHighestLevel){ // Save the modulation format with the highest level as the second choice option
							secondModulation = mod;
							secondBand = band;
							secondHighestLevel = mod.getBitsPerSymbol();
						}
					}
				}
			}
			
			// Check the modulation format options
			// If you don't have the first option, but you have the second
			// Use the second instead of the first option
			if((firstModulation == null) && (secondModulation != null)){
				firstModulation = secondModulation;
				firstBand = secondBand;
			}
			
			if(firstModulation != null){ // If there is a modulation option
				circuit.setModulation(firstModulation);
				
				// Get all circuits with links in common with the new circuit
				HashSet<Circuit> circuitList = new HashSet<Circuit>();
				for (Link link : routeTemp.getLinkList()) {
					HashSet<Circuit> circuitsAux = link.getCircuitList();
					
					for(Circuit circuitTemp : circuitsAux){
						if(!circuit.equals(circuitTemp) && !circuitList.contains(circuitTemp)){
							circuitList.add(circuitTemp);
						}
					}
				}
				
				// Search for the circuit with the worst SNR delta among the circuits with links in common with the new circuit
				double worstDeltaSNR = Double.MAX_VALUE;
				for(Circuit circuitTemp : circuitList){
					
					double deltaSNR = circuitTemp.getSNR() - circuitTemp.getModulation().getSNRthreshold();
					if(deltaSNR < worstDeltaSNR){
						worstDeltaSNR = deltaSNR;
					}
				}
				
				// It seeks to choose the route in which it was possible to select the band of the available spectrum closest to the beginning of the spectrum.
				// It also tries to choose the biggest worst SNR delta, so it avoids choosing routes with very fragile circuits
				if((firstBand[0] < chosenBand[0]) && (worstDeltaSNR >= chosenWorstDeltaSNR)){
					chosenRoute = routeTemp;
					chosenBand = firstBand;
					chosenMod = firstModulation;
					chosenWorstDeltaSNR = worstDeltaSNR;
				}
			}
		}
		
		if(chosenRoute != null){ // If there is no route chosen is because no available resource was found in any of the candidate routes
			circuit.setRoute(chosenRoute);
			circuit.setModulation(chosenMod);
			circuit.setSpectrumAssigned(chosenBand);
			
			return true;
			
		}else{
			// The request will be blocked
			// This step is to help identify what caused the request to be blocked
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
package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
 * This version pre-establish the circuit under analysis to verify the impact on other 
 * circuits already active in the network
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
public class KShortestPathsReductionQoTO  implements IntegratedRMLSAAlgorithmInterface {
	
    private static int k = 4; // Number of candidate routes
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
		
		HashMap<Modulation, Double> routeModWorstDeltaSNR = new HashMap<Modulation, Double>();
		
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
			
	    	double highestLevel = 0.0;
	    	Modulation firstModulation = null; // First modulation format option
	    	Modulation secondModulation = null; // Second modulation format option
	    	
			for(int m = 0; m < avaliableModulations.size(); m++){
				Modulation mod = avaliableModulations.get(m);
				circuit.setModulation(mod);
				
				int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp);
				
				int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit, cp);
				circuit.setSpectrumAssigned(band);
				
				if(band != null){
					if(checkRoute == null){
						checkRoute = routeTemp;
						checkMod = mod;
						checkBand = band;
					}
					
					boolean circuitQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, routeTemp, mod, band, null);
					
					if(circuitQoT){
						double circuitDeltaSNR = circuit.getSNR() - mod.getSNRthreshold();
						
						List<Circuit> circuits = new ArrayList<Circuit>();
						for (Link link : routeTemp.getLinkList()) {
							TreeSet<Circuit> circuitsAux = link.getCircuitList();
							
							for(Circuit circuitTemp : circuitsAux){
								if(!circuit.equals(circuitTemp) && !circuits.contains(circuitTemp)){
									circuits.add(circuitTemp);
								}
							}
						}
						
						boolean othersQoT = true;
						double worstDeltaSNR = Double.MAX_VALUE;
						
						for(int i = 0; i < circuits.size(); i++){
							Circuit circuitTemp = circuits.get(i);
							
							boolean QoT = cp.computeQualityOfTransmission(circuitTemp, circuit);
							double deltaSNR = circuitTemp.getSNR() - circuitTemp.getModulation().getSNRthreshold();
							
							if(deltaSNR < worstDeltaSNR){
								worstDeltaSNR = deltaSNR;
							}
							
							if(!QoT){ //request with unacceptable QoT, has the worst deltaSNR
								othersQoT = false;
								break;
							}
						}
						
						routeModWorstDeltaSNR.put(mod, worstDeltaSNR);
						
						if(othersQoT){ // if you have not made QoT inadmissible from any of the other already active circuits
							if(circuitDeltaSNR >= sigmaPair){ // Tries to choose the modulation format that respects the sigma value
								firstModulation = mod;
							}
							if(mod.getBitsPerSymbol() > highestLevel){ // Save the modulation format with the highest level as the second choice option
								secondModulation = mod;
								highestLevel = mod.getBitsPerSymbol();
							}
						}
					}
				}
			}
			
			if((firstModulation == null) && (secondModulation != null)){ // Check the modulation format options
				firstModulation = secondModulation;
			}
			
			if(firstModulation != null){
				int numberOfSlots = firstModulation.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp);
				int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit, cp);
				
				if(band != null){
					double worstDeltaSNR = routeModWorstDeltaSNR.get(firstModulation);
					
					if((band[0] < chosenBand[0]) && (worstDeltaSNR >= chosenWorstDeltaSNR)){
						chosenBand = band;
						chosenRoute = routeTemp;
						chosenMod = firstModulation;
						chosenWorstDeltaSNR = worstDeltaSNR;
					}
				}
			}
		}
		
		if(chosenRoute != null){ // If there is no route chosen is because no available resource was found in any of the candidate routes
			circuit.setRoute(chosenRoute);
			circuit.setModulation(chosenMod);
			circuit.setSpectrumAssigned(chosenBand);
			
			return true;
			
		}else{
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

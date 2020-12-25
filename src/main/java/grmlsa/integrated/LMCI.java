package grmlsa.integrated;

import java.util.HashMap;
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
import util.IntersectionFreeSpectrum;

/**
 * This class implements the integrated RMLSA algorithm Low-Margin With Circuit Invigorating (LMCI) presented in the article:
 *  - SBRC 2020 - Novo RMLSA com Tonificação de Circuito e ciente da Qualidade de Transmissão com Baixa Margem em Redes Ópticas Elásticas
 * 
 * LMCI algorithm uses the excess percentage of SNR threshold (margin).
 * This relative margin represents a percentage value in dB above the SNR threshold.
 * The value of margin must be entered in the configuration file "others" as shown below.
 * {"variables":{
 *               "margin":"2.0"
 *               }
 * }
 * 
 * @author Alexandre
 */
public class LMCI implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    // A single sigma value for all pairs
    private double margin;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
            
            //read the margin value
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			margin = Double.parseDouble((String)uv.get("margin"));
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }
        
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = null;
        
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        
        HashMap<Route, Modulation> routeMod = new HashMap<Route, Modulation>();
        HashMap<Route, int[]> routeBand = new HashMap<Route, int[]>();
        
        // To avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		
        for (Route route : candidateRoutes) {
			circuit.setRoute(route);
			
			boolean flagMod = false;
            
        	// Begins with the most spectrally efficient modulation format
    		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
    			Modulation mod = avaliableModulations.get(m);
    			
            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
	            
	            mod = circuitInvigorationg(slotsNumber, mod, circuit, avaliableModulations);
	            circuit.setModulation(mod);
	            
//	            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
	            
	            for (int[] faixa : merge) {
	            	
	            	if (faixa[1] - faixa[0] + 1 >= slotsNumber) { // Number of slots in the free band is greater than or equal to the number of slots required
	            		
	            		int band[] = faixa.clone();
	            		band[1] = band[0] + slotsNumber - 1;//It is not necessary to allocate the entire band, just the amount of slots required
	            		
	            		circuit.setSpectrumAssigned(band);
	            		
	            		checkRoute = route;
	            		checkMod = mod;
	            		checkBand = band;
		            	
	            		// Computes QoT for the new circuit
						boolean circuitQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null, false);
	            		
		            	if(circuitQoT){ //modulation has acceptable QoT
		            		
		            		double SNRthreshold = mod.getSNRthreshold();
		            		double SNRthreshold_margin = SNRthreshold + (SNRthreshold * margin);
		            		
		            		if (circuit.getSNR() >= SNRthreshold_margin) {
		            			
//			            		chosenRoute = route;
//				                chosenMod = mod;
//				                chosenBand = band;
				                
				                routeMod.put(route, mod);
				                routeBand.put(route, band.clone());
				                
				                flagMod = true;
				                
				                break;
		            		}
		            	}
		            }
	            }
	            
	            if(flagMod){
	            	break;
	            }
            }
        }
        
        int slotIndex = Integer.MAX_VALUE;
        for (Route route : routeMod.keySet()) {
        	
        	Modulation mod = routeMod.get(route);
        	int band[] = routeBand.get(route);
        	
        	if(band[0] < slotIndex) {
        		slotIndex = band[0];
        		
        		chosenRoute = route;
                chosenMod = mod;
                chosenBand = band;
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
    
    public Modulation circuitInvigorationg(int slotsNumber, Modulation mod, Circuit circuit, List<Modulation> avaliableModulations) {
    	Modulation modRes = mod;
    	
    	// Begins with the most spectrally efficient modulation format
		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
			Modulation modTemp = avaliableModulations.get(m);
			
			int slotsNumberTemp = modTemp.requiredSlots(circuit.getRequiredBandwidth());
			
			if ((slotsNumber == slotsNumberTemp) && (mod.getBitsPerSymbol() > modTemp.getBitsPerSymbol())) {
				modRes = modTemp;
			}
		}
    	
		return modRes;
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

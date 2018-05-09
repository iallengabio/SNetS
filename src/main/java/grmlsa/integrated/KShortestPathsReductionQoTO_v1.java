package grmlsa.integrated;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByQoT;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import simulationControl.Util;
import util.IntersectionFreeSpectrum;

/**
 * Based on the article:
 *  - An Efficient IA-RMLSA Algorithm for Transparent Elastic Optical Networks (2017)
 *  
 * This version does not pre-establish the circuit under analysis to verify the impact on other 
 * circuits already active in the network
 * 
 * @author Alexandre
 */
public class KShortestPathsReductionQoTO_v1 implements IntegratedRMLSAAlgorithmInterface {
	
	private NewKShortestPaths kShortestsPaths;
	//private KFixedRoutes kShortestsPaths;
	
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    // A single sigma value for all pairs
    private double sigma;
    
    // A sigma value for each pair
	private HashMap<String, HashMap<Route, Double>> sigmaForAllPairs;
	
	// Used as a separator between the names of nodes
    private static final String DIV = "-";
    
    // Number of candidate routes
    private static int k = 3;
    
	@Override
	public boolean rsa(Circuit circuit, ControlPlane cp) {
		if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
        	//kShortestsPaths = new KFixedRoutes(cp.getMesh());
        	
        	sigmaFileReader();
        }
        if (modulationSelection == null){
        	modulationSelection = new ModulationSelectionByQoT();
        	modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(cp.getMesh()));
        }
        if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}

        List<Modulation> avaliableModulations = modulationSelection.getAvaliableModulations();
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
				
				int numberOfSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp);
				int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit);
				
				if(band != null){
					if(checkRoute == null){
						checkRoute = routeTemp;
						checkMod = mod;
						checkBand = band;
					}
					
					boolean circuitQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, routeTemp, mod, band);
					
					if(circuitQoT){
						circuit.setModulation(mod);
						circuit.setSpectrumAssigned(band);
						
						try {
							cp.allocateCircuit(circuit);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //pre establishes the circuit, to check the impact on other already active circuits
						
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
							
							boolean QoT = cp.computeQualityOfTransmission(circuitTemp);
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
						
						if(othersQoT){
							if(circuitDeltaSNR >= sigmaPair){ // Tries to choose the modulation format that respects the sigma value
								firstModulation = mod;
							}
							if(mod.getBitsPerSymbol() > highestLevel){ // Save the modulation format with the highest level as the second choice option
								secondModulation = mod;
								highestLevel = mod.getBitsPerSymbol();
							}
						}
						
						try {
							cp.releaseCircuit(circuit);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //releases the resources used by the circuit
					}
				}
			}
			
			if((firstModulation == null) && (secondModulation != null)){ // Check the modulation format options
				firstModulation = secondModulation;
			}
			
			if(firstModulation != null){
				int numberOfSlots = firstModulation.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp);
				int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit);
				
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
	
	public void sigmaFileReader(){
		sigma = 0.0;
		
		String separator = System.getProperty("file.separator");
		String sigmaPathArqConfiguration = Util.projectPath + separator + "sigma.txt";
		String sigmaForAllPairspathArqConfiguration = Util.projectPath + separator + "sigmaForAllPairs.txt";
		
		if(!Paths.get(sigmaForAllPairspathArqConfiguration).toFile().exists()){
			System.err.println("\tFile with sigma value for the pairs not found. The simulation will use a sigma value for all pairs.");
			
			if(!Paths.get(sigmaPathArqConfiguration).toFile().exists()){
				System.err.println("\tFile with sigma value not found. The simulation will use a default value.");
				
			}else{
				
				try {
					FileReader fr = new FileReader(sigmaPathArqConfiguration);
					BufferedReader in = new BufferedReader(fr);
					
					String linha = in.readLine();
					sigma = Double.valueOf(linha);
					
					in.close();
				    fr.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
		}else{
			
			sigmaForAllPairs = new HashMap<>();
			
			try {
				FileReader fr = new FileReader(sigmaForAllPairspathArqConfiguration);
				BufferedReader in = new BufferedReader(fr);
				
				while(in.ready()){
					String linha[] = in.readLine().split(";");
					
					String pair = linha[0];
					HashMap<Route, Double> sigmaRoute = new HashMap<>();
					List<Route> candidateRoutes = kShortestsPaths.getRoutes(pair);
					
					for(int r = 0; r < k; r++){
						double sigmaPair = Double.valueOf(linha[1 + r]);
						Route route = candidateRoutes.get(r);
						sigmaRoute.put(route, sigmaPair);
					}
					
					sigmaForAllPairs.put(pair, sigmaRoute);
				}
				
				in.close();
			    fr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void createFileSigmaAllPairs(int numberOfNodes, String pathFile, double sigmaAllPairs[]) {
		int cont = 0;
		
		try {
			FileWriter fw = new FileWriter(pathFile);
			BufferedWriter out = new BufferedWriter(fw);
			
			for(int i = 1; i <= numberOfNodes; i++){
				StringBuilder sb = new StringBuilder();
				
				for(int j = 1; j <= numberOfNodes; j++){
					if(i != j){
						
						sb.append(i + DIV + j + ";");
						for(int r = 0; r < k; r++){
							sb.append(String.valueOf(sigmaAllPairs[cont]));
							if(r < k - 1){
								sb.append(";");
							}
							
							cont++;
						}
						sb.append("\n");
					}
				}
				out.append(sb.toString());
			}
			
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

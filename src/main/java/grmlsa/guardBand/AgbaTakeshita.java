package grmlsa.guardBand;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import simulationControl.parsers.NetworkConfig;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the implementation of the AGBA algorithm
 * presented in the article: - Adaptive Guard-Band Assignment with Adaptive
 * Spectral Profile Equalizer to Improve Spectral Usage of Tmpairment-Aware
 * Elastic Optical Network (2016)
 * 
 * In the AGBA the size of guard band is selected based on the hop count
 * of the chosen route.
 * 
 * @authors Takeshita Et al.
 */
public class AgbaTakeshita implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    public static int maxSaltos = 0;
    public static double maxUtilization = 0;
    private static final String DIV = "-";
    static String filename = "simulations/Cost239_v2_Fuzzy_GB4/tipper.fcl";
    
    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
        }
        if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}
        
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached
        
        
        for (Route route : candidateRoutes) {
            
            circuit.setRoute(route);
            
            List<Modulation> modulacoes = cp.getMesh().getAvaliableModulations();
            
            //AGBA
            for (int m = 0; m < modulacoes.size(); m++) {
            	if(route.getHops() <= 4){
            		modulacoes.get(m).setGuardBand(1);
                }else{
                    modulacoes.get(m).setGuardBand(2);
                }
            }
            
            
            cp.getMesh().setAvaliableModulations(modulacoes);
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            Modulation mod1 = null;
            try {
				mod1 = (Modulation) mod.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            circuit.setModulation(mod1);
            
            if(mod1 != null){
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, mod1.getGuardBand());
	
	            // Calculate how many slots are needed for this route
	            int ff[] = spectrumAssignment.policy(mod1.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);
	            
		            if (ff != null && ff[0] < chosenBand[0]) {
		                chosenBand = ff;
		                chosenRoute = route;
		                chosenMod = mod1;
		        
		            }
            }
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);

            return true;

        } else {
            circuit.setRoute(candidateRoutes.get(0));
            circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
            circuit.setSpectrumAssigned(null);
            
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



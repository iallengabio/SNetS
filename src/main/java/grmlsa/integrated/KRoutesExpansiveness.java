package grmlsa.integrated;

import grmlsa.*;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.ExpansivenessFit;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;
import java.util.Map;

/**
 * This class represents the implementation of the Complete Sharing algorithm presented in the article:
 *  - Spectrum management in heterogeneous bandwidth optical networks (2014)
 *  
 * In the Complete Sharing the route and the frequency slots are selected in order to allocate a range of 
 * spectrum closer to the beginning of the optical spectrum.
 * 
 * @author Iallen
 */
public class KRoutesExpansiveness implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private ExpansivenessFit spectrumAssignment;

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
			spectrumAssignment = new ExpansivenessFit();
		}

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached

        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            int bestExpasiveness = -1;
            
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            circuit.setModulation(mod);
            
            if(mod != null){
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
	
	            // Calculate how many slots are needed for this route
	            int ff[] = spectrumAssignment.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);
	            if(ff!=null){
                    int ex = spectrumAssignment.expasiveness(ff, merge);
                    if(ex>bestExpasiveness){
                        chosenBand = ff;
                        chosenRoute = route;
                        chosenMod = mod;
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

package grmlsa.integrated;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.FirstFitExpansiveness;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;
import java.util.Map;

/**
 * This class implements an RMLSA algotithm based on Complete Sharing algorithm considering the expansiveness of the circuits.
 *  
 * In the Complete Sharing the route and the frequency slots are selected in order to allocate a range of 
 * spectrum closer to the beginning of the optical spectrum.
 * 
 * @author Iallen
 */
public class CompleteSharingEx implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private int sigmaExpansiveness=0;
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private FirstFitExpansiveness spectrumAssignment;


    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
            modulationSelection = cp.getModulationSelection();
            spectrumAssignment = new FirstFitExpansiveness();
            Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
            this.sigmaExpansiveness = Integer.parseInt((String)uv.get("sigmaExpansiveness"));
        }

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached
        int sigmaAux = sigmaExpansiveness;
        while(sigmaAux>=0 && chosenRoute ==null) {
            spectrumAssignment.setFfeSigma(sigmaAux);
            for (Route route : candidateRoutes) {
                circuit.setRoute(route);
                Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
                if (mod != null) {
                    List<int[]> merge = IntersectionFreeSpectrum.merge(route);

                    // Calculate how many slots are needed for this route
                    int ff[] = spectrumAssignment.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);

                    if (ff != null && ff[0] < chosenBand[0]) {
                        chosenBand = ff;
                        chosenRoute = route;
                        chosenMod = mod;
                    }
                }
            }
            sigmaAux--;
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);

            return true;

        } else {
            circuit.setRoute(candidateRoutes.get(0));
            circuit.setModulation(modulationSelection.getAvaliableModulations().get(0));
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

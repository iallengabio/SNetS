package grmlsa.integrated;

import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * This class implements the integrated RMLSA algorithm that uses First Fit to spectrum allocation and try establish the circuit with 'k' shortests paths.
 * @author Iallen
 */
public class KSPFirstFit implements IntegratedRMLSAAlgorithmInterface{

    private int K = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), K);
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
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);

            List<int[]> merge = IntersectionFreeSpectrum.merge(route);

            // Calculate how many slots are needed for this route
            int ff[] = spectrumAssignment.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);

            if (ff != null) {
                chosenBand = ff;
                chosenRoute = route;
                chosenMod = mod;
                break;
            }
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

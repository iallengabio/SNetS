package grmlsa.integrated;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFitEsPAT;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;
import java.util.Map;

/**
 * This class implements the mechanism EsPAT over algorithm CompleteSharing.
 * EsPAT is presented at: "Um Mecanismo para Potencializa��o da Agrega��o de Tr�fego em Redes �pticas El�sticas" SBRC2019
 * 
 * @author Iallen
 */
public class CompleteSharingSSTG1 implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private int sigmaExpansiveness=0;
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private FirstFitEsPAT spectrumAssignment;


    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
            modulationSelection = cp.getModulationSelection();
            spectrumAssignment = new FirstFitEsPAT();
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
                circuit.setModulation(mod);
                
                if (mod != null) {
                    List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());

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

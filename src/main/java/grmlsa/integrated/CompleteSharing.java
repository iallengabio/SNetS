package grmlsa.integrated;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistance;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * This class represents the implementation of the Complete Sharing algorithm presented in the article:
 *  - Spectrum management in heterogeneous bandwidth optical networks (2014)
 *  
 * In the Complete Sharing the route and the frequency slots are selected in order to allocate a range of 
 * spectrum closer to the beginning of the optical spectrum.
 * 
 * @author Iallen
 */
public class CompleteSharing implements IntegratedRMLSAAlgorithmInterface {

    private NewKShortestPaths kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, Mesh mesh) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(mesh, 3); //This algorithm uses 3 alternative paths
        }
        if (modulationSelection == null){
        	modulationSelection = new ModulationSelectionByDistance();
        	modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(mesh));
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
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, mesh);

            List<int[]> merge = IntersectionFreeSpectrum.merge(route);

            // Calculate how many slots are needed for this route
            int ff[] = spectrumAssignment.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit);

            if (ff != null && ff[0] < chosenBand[0]) {
                chosenBand = ff;
                chosenRoute = route;
                chosenMod = mod;
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

}

package grmlsa.integrated;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.MultifactorSpectrumAssignment;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

import java.util.List;

public class MultifactorRSA implements IntegratedRMLSAAlgorithmInterface{

    private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private MultifactorSpectrumAssignment spectrumAssignment;


    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
            spectrumAssignment = new MultifactorSpectrumAssignment();
        }

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached

        double bestCost = Double.MAX_VALUE;


        for (Route route : candidateRoutes) {
            circuit.setRoute(route);

            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            circuit.setModulation(mod);

            if(mod != null){
                List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());

                // Calculate how many slots are needed for this route
                int ff[] = circuit.getSpectrumAssigned();

                if (ff != null) {
                    double scost = spectrumAssignment.getCost(circuit,cp);
                    if(scost<bestCost){
                        bestCost = scost;
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

    @Override
    public KRoutingAlgorithmInterface getRoutingAlgorithm() {
        return null;
    }
}

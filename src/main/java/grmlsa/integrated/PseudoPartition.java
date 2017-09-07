package grmlsa.integrated;

import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistance;
import grmlsa.modulation.ModulationSelector;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.HashSet;
import java.util.List;

/**
 * This class represents the implementation of the Pseudo Partition algorithm presented in the article:
 *  - Spectrum management in heterogeneous bandwidth optical networks (2014)
 *  
 * The Pseudo Partition divides the requests of optical circuits according to the requested bandwidth.
 * Some requests allocate frequency slots using the First Fit policy and others allocate frequency slots 
 * using the Last Fit policy.
 * 
 * @author Iallen
 */
public class PseudoPartition implements IntegratedRMLSAAlgorithmInterface {


    /**
     *
     New circuits will be allocated using @spectrumAssignment1 if their bandwidth requirement is lower than the threshold. Otherwise, they will be allocated using @spectrumAssignment2.
     */
    private Double threshold;

    private NewKShortestPaths kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment1;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment2;

    public PseudoPartition() {
        threshold = 300000000000.0; // 300Gbps
    }

    @Override
    public boolean rsa(Circuit circuit, Mesh mesh, ControlPlane cp) {
    	if(kShortestsPaths == null){
			kShortestsPaths = new NewKShortestPaths(mesh, 3); //This algorithm uses 3 alternative paths
		}
    	if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        	modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(mesh));
        }
		if(spectrumAssignment1 == null && spectrumAssignment2 == null){
			spectrumAssignment1 = new FirstFit();
			spectrumAssignment2 = new LastFit();
		}
		
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = new int[2];
        
        // Check whether the FirstFit should be applied from the bottom up or from the top down
        if (circuit.getRequiredBandwidth()<threshold) { // Allocate from bottom to top
            chosenBand[0] = 9999999;
            chosenBand[1] = 9999999;

            for (Route route : candidateRoutes) {
                
                circuit.setRoute(route);
                Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment1, mesh);

                List<int[]> merge = IntersectionFreeSpectrum.merge(route);

                // Calculate how many slots are needed for this route
                int ff[] = spectrumAssignment1.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit);

                if (ff != null && ff[0] < chosenBand[0]) {
                    chosenBand = ff;
                    chosenRoute = route;
                    chosenMod = mod;
                }
            }

        } else { //Allocate from top to bottom

            chosenBand[0] = -1;
            chosenBand[1] = -1;

            for (Route route : candidateRoutes) {
                
                circuit.setRoute(route);
                Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment2, mesh);

                List<int[]> merge = IntersectionFreeSpectrum.merge(route);

                // Calculate how many slots are needed for this route
                int lf[] = spectrumAssignment2.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit);

                if (lf != null && lf[1] > chosenBand[1]) {
                    chosenBand = lf;
                    chosenRoute = route;
                    chosenMod = mod;
                }
            }

        }

        if (chosenRoute != null) { // If there is no route chosen is why no available resource was found on any of the candidate routes
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

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
import network.ControlPlane;
import network.Link;
import network.Mesh;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class presents a proposal for modification in the Dedicated Partition algorithm.
 * 
 * @author Iallen
 */
public class LoadBalancedDedicatedPartition implements IntegratedRMLSAAlgorithmInterface {

    private NewKShortestPaths kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    private HashMap<Integer, int[]> zones;

    public LoadBalancedDedicatedPartition() {
        List<int[]> zones = null;
        try {
            //zones = ZonesFileReader.readTrafic(Util.projectPath + "/zones");
        } catch (Exception e) {
            System.err.println("It was not possible to read the file with the zones specification!");
            
            e.printStackTrace();
        }
        this.zones = new HashMap<>();
        int aux[];
        for (int[] zone : zones) {
            aux = new int[2];
            aux[0] = zone[1];
            aux[1] = zone[2];
            this.zones.put(zone[0], aux);
        }
    }

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
    	if(kShortestsPaths == null){
			kShortestsPaths = new NewKShortestPaths(cp.getMesh(), 3); //This algorithm uses 3 alternative paths
		}
    	if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        	modulationSelection.setAvaliableModulations(ModulationSelector.configureModulations(cp.getMesh()));
        }
		if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached
        int leastUsed = 999999999;

        for (Route route : candidateRoutes) {
            
            circuit.setRoute(route);
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);

            // Calculate how many slots are needed for this route
            int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
            int zone[] = this.zones.get(numSlots);
            List<int[]> primaryZone = new ArrayList<>();
            primaryZone.add(zone);

            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
            merge = IntersectionFreeSpectrum.merge(merge, primaryZone);

            int ff[] = spectrumAssignment.policy(numSlots, merge, circuit, cp);

            int ut = this.numSlotsUsedZone(route, zone);

            if (ff != null && ut < leastUsed) {
                chosenBand = ff;
                chosenRoute = route;
                leastUsed = ut;
                chosenMod = mod;
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

    /**
     * Returns the sum of the square of the number of slots used in each link of a route in a given zone
     *
     * @param route Route
     * @param zone int[]
     * @return int
     */
    private int numSlotsUsedZone(Route route, int zone[]) {
        int res = 0;
        List<int[]> zoneAux = new ArrayList<int[]>();
        zoneAux.add(zone);

        for (Link link : route.getLinkList()) {
            List<int[]> merge = IntersectionFreeSpectrum.merge(link.getFreeSpectrumBands(), zoneAux);

            int free = 0;
            for (int[] is : merge) {
                free += (is[1] - is[0] + 1);
            }
            int used = link.getNumOfSlots() - free;

            res += (used * used);

        }

        return res;
    }

}

package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
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
 * This class presents a proposal for Zone Partition algorithm.
 * 
 * @author Iallen
 */
public class ZonePartition implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    private HashMap<Integer, int[]> zones;

    public ZonePartition() {
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

        //Try to allocate in the primary zone
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            circuit.setModulation(mod);
            
            if(mod != null){
	            
	            // Calculate how many slots are needed for this route
	            int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
	            int zone[] = this.zones.get(numSlots);
	            List<int[]> primaryZone = new ArrayList<>();
	            primaryZone.add(zone);
	
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
	            merge = IntersectionFreeSpectrum.merge(merge, primaryZone);
	
	            int ff[] = spectrumAssignment.policy(numSlots, merge, circuit, cp);
	
	            if (ff != null && ff[0] < chosenBand[0]) {
	                chosenBand = ff;
	                chosenRoute = route;
	                chosenMod = mod;
	            }
            }
        }

        //If no resource could be allocated, try the secondary zone
        if (chosenRoute == null) {
            for (Route r : candidateRoutes) {
                circuit.setRoute(r);
                
                Modulation mod = modulationSelection.selectModulation(circuit, r, spectrumAssignment, cp);
                circuit.setModulation(mod);
                
                if(mod != null){
                	
	                // calculate how many slots are needed for this route
	                int numSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
	                int zone[] = this.zones.get(numSlots);
	                List<int[]> secondaryZone = this.secondaryZone(zone, r.getLinkList().get(0).getNumOfSlots());
	
	                List<int[]> merge = IntersectionFreeSpectrum.merge(r, circuit.getGuardBand());
	                merge = IntersectionFreeSpectrum.merge(merge, secondaryZone);
	
	                int ff[] = spectrumAssignment.policy(numSlots, merge, circuit, cp);
	
	                if (ff != null && ff[0] < chosenBand[0]) {
	                    chosenBand = ff;
	                    chosenRoute = r;
	                    chosenMod = mod;
	                }
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
            circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
            circuit.setSpectrumAssigned(null);
            
            return false;
        }

    }

    /**
     * Passes a zone as a parameter and returns a list of free tracks corresponding to the complementary zone
     *
     * @param zone int[]
     * @param numberOfSlots int
     * @return List<int[]>
     */
    private List<int[]> secondaryZone(int[] zone, int numberOfSlots) {

        int beginning = zone[0];
        int end = zone[1];

        List<int[]> res = new ArrayList<>();
        int aux[] = null;
        if (beginning > 1) {
            aux = new int[2];
            aux[0] = 1;
            aux[1] = beginning - 1;
            res.add(aux);
        }

        if (end < numberOfSlots) {
            aux = new int[2];
            aux[0] = end + 1;
            aux[1] = 400;
            res.add(aux);
        }

        return res;
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

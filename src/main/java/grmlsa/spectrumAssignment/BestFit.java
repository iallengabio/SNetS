package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the spectrum allocation technique called Best Fit.
 * This technique chooses the shortest free spectrum band that accommodates the request.
 *
 * @author Iallen
 */
public class BestFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
        }

        int chosen[] = policy(numberOfSlots, composition, circuit);

        if (chosen == null) return false;

        circuit.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum BestFit
     * 
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @param circuit Circuit
     * @return int[]
     */
    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit){
    	int chosen[] = null;
        int lessDifference = 999999999;
        
        for (int[] band : freeSpectrumBands) {
            int sizeBand = band[1] - band[0] + 1;
            if (sizeBand >= numberOfSlots) {
                if (sizeBand - numberOfSlots < lessDifference) { //Found a range with the number of slots closer to the requested quantity
                    chosen = band.clone();
                    chosen[1] = chosen[0] + numberOfSlots - 1; //It is not necessary to allocate the entire band, just the amount of slots required
                    lessDifference = sizeBand - numberOfSlots;
                }
            }
        }

        return chosen;
	}

}


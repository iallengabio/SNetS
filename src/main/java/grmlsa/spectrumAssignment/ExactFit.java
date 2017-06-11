package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents the spectrum allocation technique called Exact Fit.
 * This technique attempts to allocate a range of spectrum with exactly the same size as the number of slots required by the new request.
 * If the spectrum band is not found, the request is allocated following the worst fit policy.
 *
 * @author Felipe
 */

public class ExactFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit request) {
        Route route = request.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
        }

        int chosen[] = exactFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum ExactFit
     *
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @return int[]
     */
    private static int[] exactFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        
        for (int[] band : freeSpectrumBands) {
            int sizeBand = band[1] - band[0] + 1;
            if (chosen == null) {
                if (sizeBand == numberOfSlots) {
                    chosen = band;
                    chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                }
            }
        }

        if (chosen == null) {// did not find any contiguous tracks and is still available
            // now just look for the free range with size farthest from the amount of slots required

            int greaterDifference = -1;
            for (int[] band : freeSpectrumBands) {
                int sizeBand = band[1] - band[0] + 1;
                if (sizeBand >= numberOfSlots) {
                    if (sizeBand - numberOfSlots > greaterDifference) { //Found a band with more slots quantity "different"
                        chosen = band;
                        chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                        greaterDifference = sizeBand - numberOfSlots;
                    }
                }
            }
        }

        return chosen;
    }

}

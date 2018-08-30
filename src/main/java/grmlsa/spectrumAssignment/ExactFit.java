package grmlsa.spectrumAssignment;

import java.util.List;

import network.Circuit;
import util.IntersectionFreeSpectrum;


/**
 * This class represents the spectrum allocation technique called Exact Fit.
 * This technique attempts to allocate a range of spectrum with exactly the same size as the number of slots required by the new request.
 * If the spectrum band is not found, the request is allocated following the worst fit policy.
 *
 * @author Felipe
 */

public class ExactFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit) {
    	List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit);

        if (chosen == null) return false;

        circuit.setSpectrumAssigned(chosen);

        return true;
    }
    
    /**
     * Applies the policy of allocation of spectrum ExactFit
     * 
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @param circuit Circuit
     * @return int[]
     */
    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit){
    	int chosen[] = null;
        
        for (int[] band : freeSpectrumBands) {
            int sizeBand = band[1] - band[0] + 1;
            if (chosen == null) {
                if (sizeBand == numberOfSlots) {
                    chosen = band.clone();
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
                        chosen = band.clone();
                        chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                        greaterDifference = sizeBand - numberOfSlots;
                    }
                }
            }
        }

        return chosen;
    }

}

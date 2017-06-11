package grmlsa.spectrumAssignment;

import network.Circuit;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * This class represents the spectrum allocation technique called First Fit.
 * This technique chooses the first free spectrum band that accommodates the request.
 *
 * @author Iallen
 */
public class FirstFit implements SpectrumAssignmentAlgorithmInterface {

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit request) {
    	
        List<int[]> composition = IntersectionFreeSpectrum.merge(request.getRoute());

        int chosen[] = firstFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum FirstFit
     * 
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @return int[]
     */
    public static int[] firstFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        for (int[] band : freeSpectrumBands) {
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[1] = chosen[0] + numberOfSlots - 1;//It is not necessary to allocate the entire band, just the amount of slots required
                break;
            }
        }
        return chosen;
    }

}


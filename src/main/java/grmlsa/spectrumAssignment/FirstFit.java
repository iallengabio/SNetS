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
public class FirstFit implements SpectrumAssignmentInterface {


    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit request) {
    	
        List<int[]> composition = IntersectionFreeSpectrum.merge(request.getRoute());

        int chosen[] = firstFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     *
     * @param numberOfSlots
     * @param freeSpectrumBands
     * @return
     */
    public static int[] firstFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        for (int[] band : freeSpectrumBands) {
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[1] = chosen[0] + numberOfSlots - 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria
                break;
            }
        }

        return chosen;
    }


}


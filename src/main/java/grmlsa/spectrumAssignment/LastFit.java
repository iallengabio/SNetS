package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the spectrum allocation technique called First Fit.
 * This technique chooses the last free spectrum band that accommodates the request.
 *
 * @author Iallen
 */
public class LastFit implements SpectrumAssignmentAlgorithmInterface {

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

        int chosen[] = lastFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum LastFit
     *
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @return int[]
     */
    public static int[] lastFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        int band[] = null;
        int i;
        
        for (i = freeSpectrumBands.size() - 1; i >= 0; i--) {
            band = freeSpectrumBands.get(i);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[0] = chosen[1] - numberOfSlots + 1;//It is not necessary to allocate the entire band, just the amount of slots required

                break;
            }
        }

        return chosen;
    }

}


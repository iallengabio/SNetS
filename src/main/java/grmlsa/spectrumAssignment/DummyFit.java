package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the spectrum allocation technique called Dummy Fit.
 * The objective of this class is to just exemplify the implementation of a spectrum allocation algorithm.
 *
 * @author Iallen
 */
public class DummyFit implements SpectrumAssignmentAlgorithmInterface {

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

        int chosen[] = dummyFit(numberOfSlots, composition);

        if (chosen == null) return false;

        circuit.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * Applies the policy of allocation of spectrum DummyFit
     *
     * @param numberOfSlots int
     * @param freeSpectrumBands List<int[]>
     * @return int[]
     */
    private static int[] dummyFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = new int[2];

        if (freeSpectrumBands.size() >= 1) {
            int band[] = freeSpectrumBands.get(0);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen[0] = band[0];
                chosen[1] = chosen[0] + numberOfSlots - 1;
                return chosen;
            }
        }
        return null;
    }

}


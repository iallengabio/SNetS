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
public class DummyFit implements SpectrumAssignmentAlgoritm {

    public DummyFit() {

    }

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit request) {
        Route route = request.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;
        IntersectionFreeSpectrum ifs = new IntersectionFreeSpectrum();
        for (i = 1; i < links.size(); i++) {
            composition = ifs.merge(composition, links.get(i).getFreeSpectrumBands());
        }

        int chosen[] = dummyFit(numberOfSlots, composition);

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
    private static int[] dummyFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = new int[2];

        if (freeSpectrumBands.size() >= 1) {
            int faixa[] = freeSpectrumBands.get(0);
            if (faixa[1] - faixa[0] + 1 >= numberOfSlots) {
                chosen[0] = faixa[0];
                chosen[1] = chosen[0] + numberOfSlots - 1;
                return chosen;
            }


        }
        return null;
    }


}


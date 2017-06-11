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
public class LastFit implements SpectrumAssignmentIterface {

    public LastFit() {

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

        int chosen[] = lastFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     *
     *
     * @param numberOfSlots
     * @param freeSpectrumBands
     * @return
     */
    public static int[] lastFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        int band[] = null;
        int i;
        for (i = freeSpectrumBands.size() - 1; i >= 0; i--) {
            band = freeSpectrumBands.get(i);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[0] = chosen[1] - numberOfSlots + 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria

                break;
            }
        }

        return chosen;
    }


}


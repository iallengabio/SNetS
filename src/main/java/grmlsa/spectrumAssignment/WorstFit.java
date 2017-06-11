package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the spectrum allocation technique called Best Fit.
 * This technique chooses the biggest free spectrum band that accommodates the request.
 * @author Iallen
 */
public class WorstFit implements SpectrumAssignmentInterface {

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

        int chosen[] = null;
        int maiorDif = -1;
        for (int[] band : composition) {
            int tamFaixa = band[1] - band[0] + 1;
            if (tamFaixa >= numberOfSlots) {
                if (tamFaixa - numberOfSlots > maiorDif) {
                    chosen = band;
                    chosen[1] = chosen[0] + numberOfSlots - 1;
                    maiorDif = tamFaixa - numberOfSlots;
                }
            }
        }

        request.setSpectrumAssigned(chosen);

        return true;
    }


}


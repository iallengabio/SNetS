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
public class BestFit implements SpectrumAssignmentIterface {

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


        int chosen[] = bestFit(numberOfSlots, composition);

        if (chosen == null) return false;

        request.setSpectrumAssigned(chosen);

        return true;
    }

    private static int[] bestFit(int numberOfSlots, List<int[]> freeSpectrumBands) {
        int chosen[] = null;
        int menorDif = 999999999;
        for (int[] band : freeSpectrumBands) {
            int tamFaixa = band[1] - band[0] + 1;
            if (tamFaixa >= numberOfSlots) {
                if (tamFaixa - numberOfSlots < menorDif) { //encontrou uma faixa com quantidade de slots mais pr�xima da quantidade requisitada
                    chosen = band;
                    chosen[1] = chosen[0] + numberOfSlots - 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria
                    menorDif = tamFaixa - numberOfSlots;
                }
            }
        }

        return chosen;
    }


}


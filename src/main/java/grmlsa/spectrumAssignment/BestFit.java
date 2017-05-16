package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta � uma classe que faz atribui��o de espectro seguindo a pol�tica first fit
 *
 * @author Iallen
 */
public class BestFit implements SpectrumAssignmentAlgoritm {

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

        //agora basta buscar a faixa livre com tamanho mais pr�ximo da quantidade de slots requisitados
        int chosen[] = bestFit(numberOfSlots, composition);

        if (chosen == null) return false; //n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel

        request.setSpectrumAssigned(chosen);

        return true;
    }

    public static int[] bestFit(int numberOfSlots, List<int[]> livres) {
        int chosen[] = null;
        int menorDif = 999999999;
        for (int[] band : livres) {
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


    private void printFreeSpectrumBands(List<int[]> links) {
        for (int[] is : links) {

        }
    }

}


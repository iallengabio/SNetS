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
public class LastFit implements SpectrumAssignmentAlgoritm {

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

        //agora basta buscar a primeira faixa livre com a quantidade de slots requsiitadas na composi��o gerada
        int chosen[] = lastFit(numberOfSlots, composition);

        if (chosen == null) return false; //n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * aplica a pol�tica lastFit a uma determinada lista de faixas livres retorna a faixa escolhida
     *
     * @param numberOfSlots
     * @param livres
     * @return
     */
    public static int[] lastFit(int numberOfSlots, List<int[]> livres) {
        int chosen[] = null;
        int band[] = null;
        int i;
        for (i = livres.size() - 1; i >= 0; i--) {
            band = livres.get(i);
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[0] = chosen[1] - numberOfSlots + 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria

                break;
            }
        }

        return chosen;
    }


}


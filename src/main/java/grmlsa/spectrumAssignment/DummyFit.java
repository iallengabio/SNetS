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

        //agora basta buscar a primeira faixa livre com a quantidade de slots requsiitadas na composi��o gerada
        int chosen[] = dummyFit(numberOfSlots, composition);

        if (chosen == null) return false; //n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * aplica a pol�tica firstFit a uma determinada lista de faixas livres retorna a faixa escolhida
     *
     * @param numberOfSlots
     * @param livres
     * @return
     */
    public static int[] dummyFit(int numberOfSlots, List<int[]> livres) {
        int chosen[] = new int[2];

        if (livres.size() >= 1) {
            int faixa[] = livres.get(0);
            if (faixa[1] - faixa[0] + 1 >= numberOfSlots) {
                chosen[0] = faixa[0];
                chosen[1] = chosen[0] + numberOfSlots - 1;
                return chosen;
            }


        }
        return null;
    }


}


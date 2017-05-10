package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.List;


/**
 * Esta classe faz a atribuicao de espectro seguindo a politica exact fit
 *
 * @author Felipe
 */

public class ExactFit implements SpectrumAssignmentAlgoritm {

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

        //primeiro busca-se a faixa livre com tamanho igual ao da quantidade de slots requisitados
        int chosen[] = exactFit(numberOfSlots, composition);

        if (chosen == null) return false; //n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel

        request.setSpectrumAssigned(chosen);

        return true;
    }

    /**
     * aplica a pol�tica exactFit a uma determinada lista de faixas livres retorna a faixa escolhida
     *
     * @param numberOfSlots
     * @param livres
     * @return
     */
    public static int[] exactFit(int numberOfSlots, List<int[]> livres) {
        int chosen[] = null;
        for (int[] band : livres) {
            int tamFaixa = band[1] - band[0] + 1;
            if (chosen == null) {
                if (tamFaixa == numberOfSlots) {
                    chosen = band;
                    chosen[1] = chosen[0] + numberOfSlots - 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria

                }
            }
        }

        if (chosen == null) {//n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel
            //agora basta buscar a faixa livre com tamanho mais distante da quantidade de slots requisitados

            int maiorDif = -1;
            for (int[] band : livres) {
                int tamFaixa = band[1] - band[0] + 1;
                if (tamFaixa >= numberOfSlots) {
                    if (tamFaixa - numberOfSlots > maiorDif) { //encontrou uma faixa com quantidade de slots mais "diferente"
                        chosen = band;
                        chosen[1] = chosen[0] + numberOfSlots - 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria
                        maiorDif = tamFaixa - numberOfSlots;
                    }
                }
            }
        }

        return chosen;

    }

}

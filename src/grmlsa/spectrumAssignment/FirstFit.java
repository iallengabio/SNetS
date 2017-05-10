package grmlsa.spectrumAssignment;

import network.Circuit;
import util.IntersectionFreeSpectrum;

import java.util.List;

/**
 * Esta � uma classe que faz atribui��o de espectro seguindo a pol�tica first fit
 *
 * @author Iallen
 */
public class FirstFit implements SpectrumAssignmentAlgoritm {

    public FirstFit() {

    }

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit request) {

        List<int[]> composition = IntersectionFreeSpectrum.merge(request.getRoute());


        //agora basta buscar a primeira faixa livre com a quantidade de slots requsiitadas na composi��o gerada
        int chosen[] = firstFit(numberOfSlots, composition);

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
    public static int[] firstFit(int numberOfSlots, List<int[]> livres) {
        int chosen[] = null;
        for (int[] band : livres) {
            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band;
                chosen[1] = chosen[0] + numberOfSlots - 1;//n�o � necess�rio alocar a faixa inteira, apenas a quantidade de slots necess�ria
                break;
            }
        }

        return chosen;
    }


}


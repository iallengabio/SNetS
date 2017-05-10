package util;

import java.util.List;

public class CalculadorFragmentacao {

    public double fragmentacaoExterna(List<int[]> freeSpectrumBands, int quantSlots) {
        int aux[] = {0, 0};
        double totalFree = 0.0;
        for (int[] is : freeSpectrumBands) {
            if (is[1] - is[0] > aux[1] - aux[0]) {
                aux = is;
            }
            totalFree = totalFree + (is[1] - is[0]);

        }
        double maior = aux[1] - aux[0];

        double fe = 1 - (maior / totalFree);

        if (totalFree == 0.0) fe = 0.0; //se o espectro estiver completamente preenchido ele não está fragmentado

        return fe;
    }

    /**
     * Este método calcula a fragmentação relativa
     *
     * @param freeSpectrumBands lista de faixas livres
     * @param c                 quantidade de slots que serão alocados (valor relativo)
     * @return
     */
    public double fragmentacaoRelativa(List<int[]> freeSpectrumBands, int c) {

        int freeC = 0;
        int totalLivre = 0;
        for (int[] faixa : freeSpectrumBands) {
            int auxT = (faixa[1] - faixa[0] + 1);
            int auxF = auxT / c;
            freeC += auxF;
            totalLivre += auxT;
        }
        double f_c = 1 - ((double) (c * freeC)) / ((double) totalLivre);

        if (totalLivre == 0) f_c = 0.0;

        return f_c;
    }

}

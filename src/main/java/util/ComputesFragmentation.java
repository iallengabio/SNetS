package util;

import java.util.List;

/**
 * This class computes external fragmentation and relative fragmentation.
 * 
 * @author Iallen
 */
public class ComputesFragmentation {

	/**
	 * This method calculates the external fragmentation
	 * 
	 * @param freeSpectrumBands List<int[]> free spectrum band list     
	 * @return double
	 */
    public double externalFragmentation(List<int[]> freeSpectrumBands) {
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

        if (totalFree == 0.0) fe = 0.0; // If the spectrum is completely filled it is not fragmented

        return fe;
    }

    /**
     * This method calculates the relative fragmentation
     *
     * @param freeSpectrumBands free spectrum band list
     * @param c                 Number of slots to allocate (relative value)
     * @return					double
     */
    public double relativeFragmentation(List<int[]> freeSpectrumBands, int c) {

        int freeC = 0;
        int totalFree = 0;
        for (int[] faixa : freeSpectrumBands) {
            int auxT = (faixa[1] - faixa[0] + 1);
            int auxF = auxT / c;
            freeC += auxF;
            totalFree += auxT;
        }
        double f_c = 1 - ((double) (c * freeC)) / ((double) totalFree);

        if (totalFree == 0) f_c = 0.0;

        return f_c;
    }

}

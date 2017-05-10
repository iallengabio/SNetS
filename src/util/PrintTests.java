package util;

import java.util.List;

public class PrintTests {

    public static String printFreeSpectrum(List<int[]> faixas) {
        String res = "";

        for (int[] is : faixas) {
            res = res + "<" + is[0] + "-" + is[1] + ">" + " ";
        }

        return res;
    }

    public static String printFaixa(int faixa[]) {
        String res = "";


        res = res + "<" + faixa[0] + "-" + faixa[1] + ">";


        return res;
    }

}

package util;

import grmlsa.Route;
import network.Link;

import java.util.ArrayList;
import java.util.List;

public class IntersectionFreeSpectrum {

    /**
     * Esta funï¿½ï¿½o retorna uma lista de espectros disponï¿½veis em ambas as listas passadas por parï¿½metro
     *
     * @param l1
     * @param l2
     * @return
     */
    public static List<int[]> merge(List<int[]> l1, List<int[]> l2) {
        List<int[]> res = new ArrayList<>();

        int indL1 = 0;
        int indL2 = 0;

        int aux1[] = null;
        int aux2[] = null;
        int aux3[];

        while (indL1 < l1.size() && indL2 < l2.size()) {

            if (aux1 == null) aux1 = l1.get(indL1).clone();
            if (aux2 == null) aux2 = l2.get(indL2).clone();
            aux3 = new int[2];

            if (aux1[0] >= aux2[0]) {
                aux3[0] = aux1[0];
            } else {
                aux3[0] = aux2[0];
            }

            if (aux1[1] < aux2[0]) { //intervalos nï¿½o se sobrepï¿½e, pegar a prï¿½ximo intervalor livre na lista 1
                indL1++;
                aux1 = null;
                continue;
            }

            if (aux2[1] < aux1[0]) { //intervalos nï¿½o se sobrepï¿½e, pegar a prï¿½ximo intervalor livre na lista 1
                indL2++;
                aux2 = null;
                continue;
            }

            if (aux1[1] < aux2[1]) {
                aux3[1] = aux1[1];
                aux2[0] = aux1[1] + 1;
                indL1++;
                aux1 = null;
                res.add(aux3);
                continue;
            }
            if (aux2[1] < aux1[1]) {
                aux3[1] = aux2[1];
                aux1[0] = aux2[1] + 1;
                indL2++;
                aux2 = null;
                res.add(aux3);
                continue;
            }


            if (aux1[1] == aux2[1]) {
                aux3[1] = aux2[1];
                indL1++;
                indL2++;
                aux1 = null;
                aux2 = null;
                res.add(aux3);
            }


        }

        return res;
    }

    /**
     * retorna uma lista de espectros disponíveis em todos os enlaces da rota passada por parâmetro
     *
     * @param route
     * @return
     */
    public static List<int[]> merge(Route route) {
        List<Link> links = new ArrayList<>(route.getLinkList());
        List<int[]> composition;
        composition = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands());
        }

        return composition;
    }

    /*
     * retorna a faixa adjacente superior à faixa passada por parametro.
     * utilizado em algoritmos de agregação óptica
     */
    public static int[] faixaAdjacenteInferior(int faixa[], List<int[]> faixasLivres) {

        for (int[] fl : faixasLivres) {
            if (fl[1] == (faixa[0] - 1)) {
                return fl;
            }
        }

        return null;
    }

    /*
     * retorna a faixa adjacente superior à faixa passada por parametro.
     * utilizado em algoritmos de agregação óptica
     */
    public static int[] faixaAdjacenteSuperior(int faixa[], List<int[]> faixasLivres) {

        for (int[] fl : faixasLivres) {
            if (fl[0] == (faixa[1] + 1)) {
                return fl;
            }
        }

        return null;
    }

}

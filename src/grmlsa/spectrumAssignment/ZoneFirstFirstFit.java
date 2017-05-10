package grmlsa.spectrumAssignment;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import util.IntersectionFreeSpectrum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Esta classe representa um algoritmo de aloca��o de espectro que utiliza a pol�tica de zonas de espectro
 * os algoritmos utilizados s�o o FirstFit na zona prim�ria e o First na Zona secund�ria
 *
 * @author Iallen
 */
public class ZoneFirstFirstFit implements SpectrumAssignmentAlgoritm {

    private HashMap<Integer, int[]> zones;

    /**
     * no construtor devem ser passadas por par�metro as zonas de divis�o do espectro
     * cada zona � descrita por um vetor de 3 posi��es sendo estas respectivamente:
     * zone[0]: quantidade de espectro requerida
     * zone[1]: inicio da zona
     * zone[2]: fim da zona
     */
    public ZoneFirstFirstFit() {
        List<int[]> zones = null;
        try {
            //zones = ZonesFileReader.readTrafic(Util.projectPath + "/zones");
        } catch (Exception e) {
            System.out.println("n�o foi poss�vel ler o arquivo com a especifica��o das zonas!");

            //e.printStackTrace();
        }
        this.zones = new HashMap<>();

        for (int[] zone : zones) {
            this.zones.put(zone[0], zone);
        }
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


        int chosen[] = null;
        //primeiro buscar na faixa prim�ria utilizando o firstFit
        List<int[]> lPrimaryZone = new ArrayList<>();
        int aux[] = new int[2];
        aux[0] = zones.get(numberOfSlots)[1];
        aux[1] = zones.get(numberOfSlots)[2];
        lPrimaryZone.add(aux);
        List<int[]> comp1 = ifs.merge(composition, lPrimaryZone);
        //System.out.print("zona prim�ria: " + PrintTests.printFreeSpectrum(lPrimaryZone) );
        //System.out.print("      merge: " + PrintTests.printFreeSpectrum(comp1) );
        chosen = FirstFit.firstFit(numberOfSlots, comp1);
        //System.out.println("      escolhida: " + PrintTests.printFaixa(chosen) );


        if (chosen == null) {//buscar na faixa secund�ria utilizando o firstfit
            List<int[]> lSecondaryZone = secondaryZone(this.zones.get(numberOfSlots), links.get(0).getNumOfSlots());
            comp1 = ifs.merge(composition, lSecondaryZone);
            //System.out.print("zona secund�ria: " + PrintTests.printFreeSpectrum(lSecondaryZone) );
            //System.out.print("      merge: " + PrintTests.printFreeSpectrum(comp1) );
            chosen = FirstFit.firstFit(numberOfSlots, comp1);
            //System.out.println("      escolhida: " + PrintTests.printFaixa(chosen) );
        }

        if (chosen == null) return false; //n�o encontrou nenhuma faixa cont�gua e cont�nua dispon�vel

        request.setSpectrumAssigned(chosen);

        return true;
    }


    /**
     * passa uma zona como par�metro e � retornada uma lista de faixas livres correspondente � zona complementar
     *
     * @param zone
     * @return
     */
    private List<int[]> secondaryZone(int[] zone, int totalOfSlots) {

        int ini = zone[1];
        int fim = zone[2];

        List<int[]> res = new ArrayList<>();
        int aux[] = null;
        if (ini > 1) {
            aux = new int[2];
            aux[0] = 1;
            aux[1] = ini - 1;
            res.add(aux);
        }

        if (fim < totalOfSlots) {
            aux = new int[2];
            aux[0] = fim + 1;
            aux[1] = 400;
            res.add(aux);
        }

        return res;
    }


}


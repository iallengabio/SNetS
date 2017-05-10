package measurement;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;

import java.util.HashMap;
import java.util.Set;


public class UtilizacaoSpectro extends Measurement {
    public final static String SEP = "-";

    private Mesh mesh;

    private double utilizationGen;
    private int obsUtilizacao;
    private HashMap<String, Double> utilizationPerLink;


    private int[] desUtilizationPerSlot;


    public UtilizacaoSpectro(int loadPoint, int replication, Mesh mesh) {
        super(loadPoint, replication);
        this.mesh = mesh;
        utilizationGen = 0.0;
        obsUtilizacao = 0;
        utilizationPerLink = new HashMap<String, Double>();

        desUtilizationPerSlot = new int[401]; //cuidado ao utilizar links com quantidades de slots diferentes de 400

    }

    /**
     * adiciona uma nova observação de utilização
     *
     * @param request
     */
    @Deprecated
    public void addNewObservation(Circuit request) {
        this.newObsUtilization();
    }

    /**
     * adiciona uma nova observação de utilização
     */
    public void addNewObservation() {
        this.newObsUtilization();
    }


    /**
     * observação de utilização do recurso espectral da rede
     */
    private void newObsUtilization() {
        //utilização geral e por link
        Double utGeral = 0.0;
        Double utLink;
        for (Link link : mesh.getLinkList()) {
            utGeral += link.getUtilization();

            utLink = this.utilizationPerLink.get(link.getSource().getName() + SEP + link.getDestination().getName());
            if (utLink == null) utLink = 0.0;
            utLink += link.getUtilization();
            this.utilizationPerLink.put(link.getSource().getName() + SEP + link.getDestination().getName(), utLink);

            //calcular desutilização por slot
            for (int[] faixa : link.getFreeSpectrumBands()) {
                incrementarDesUtFaixa(faixa);
            }

        }

        utGeral = utGeral / (double) mesh.getLinkList().size();

        this.utilizationGen += utGeral;

        this.obsUtilizacao++;


    }


    private void incrementarDesUtFaixa(int faixa[]) {
        int i;
        for (i = faixa[0] - 1; i < faixa[1]; i++) {
            desUtilizationPerSlot[i]++;
        }
    }


    public Set<String> getLinkSet() {
        return this.utilizationPerLink.keySet();
    }


    public double getUtilizationGen() {
        return this.utilizationGen / (double) this.obsUtilizacao;
    }

    public double getUtilizationPerLink(String link) {
        return this.utilizationPerLink.get(link) / (double) this.obsUtilizacao;
    }

    public double getUtilizationPerSlot(int Slot) {
        double desUt = (double) desUtilizationPerSlot[Slot - 1] / ((double) this.obsUtilizacao * mesh.getLinkList().size());

        return 1 - desUt;
    }


}

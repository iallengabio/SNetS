package measurement;

import network.Circuit;
import network.Link;
import network.Pair;
import request.RequestForConexion;
import util.IntersectionFreeSpectrum;

import java.util.HashMap;
import java.util.List;

/**
 * Esta classe representa a m�trica de probabilidade de bloqueio (geral, por
 * par, por largura de banda, por par/larguraDeBanda) A m�trica representada
 * por esta classe est� associada a um ponto de carga e uma replica��o
 *
 * @author Iallen
 */
public class ProbabilidadeDeBloqueio extends Measurement {

    public final static String SEP = "-";

    // probabilidade de bloqueio geral
    private int numReqGenGeral;
    private int numReqBlockGeral;
    private int numReqBlockGeralFragment;
    private int numReqBlockGenLackTransmitters;
    private int numReqBlockGenLackReceivers;

    // probabilidade de bloqueio por par
    private HashMap<String, Integer> numReqGenPair;
    private HashMap<String, Integer> numReqBlockPair;

    // probabilidade de bloqueio por largura de banda
    private HashMap<Double, Integer> numReqGenBW;
    private HashMap<Double, Integer> numReqBlockBW;

    // probabilidade de bloqueio por par/larguraDeBanda
    private HashMap<String, HashMap<Double, Integer>> numReqGenPairBW;
    private HashMap<String, HashMap<Double, Integer>> numReqBlockPairBW;

    public ProbabilidadeDeBloqueio(int loadPoint, int rep) {
        super(loadPoint, rep);

        this.numReqGenBW = new HashMap<>();
        this.numReqBlockBW = new HashMap<>();
        this.numReqGenPair = new HashMap<>();
        this.numReqBlockPair = new HashMap<>();
        this.numReqGenPairBW = new HashMap<>();
        this.numReqBlockPairBW = new HashMap<>();
    }

    /**
     * adiciona uma nova observa��o de bloqueio ou n�o de uma
     * requisi��o
     *
     * @param sucess
     * @param request
     */
    public void addNewObservation(boolean sucess, RequestForConexion request) {

        // incrementar requisi��es geradas geral
        this.numReqGenGeral++;
        // incrementar requisi��es geradas por par
        Integer i = this.numReqGenPair
                .get(request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName());
        if (i == null)
            i = 0;
        this.numReqGenPair.put(
                request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName(), i + 1);
        // incrementar requisi��es geradas por largura de banda
        i = this.numReqGenBW.get(request.getRequiredBandwidth());
        if (i == null)
            i = 0;
        this.numReqGenBW.put(request.getRequiredBandwidth(), i + 1);
        // incrementar requisi��es geradas por par/larguraDeBanda
        HashMap<Double, Integer> gplb = this.numReqGenPairBW
                .get(request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName());
        if (gplb == null) {
            gplb = new HashMap<>();
            this.numReqGenPairBW.put(
                    request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName(), gplb);
        }
        i = gplb.get(request.getRequiredBandwidth());
        if (i == null)
            i = 0;
        gplb.put(request.getRequiredBandwidth(), i + 1);

        // caso haja bloqueio
        if (!sucess) {
            // incrementar requisicoes bloqueadas geral
            this.numReqBlockGeral++;

            if (request.getPair().getSource().getTxs().isFullUtilized()) {// verificar se
                // a causa do
                // bloqueio foi
                // a falta de
                // transmissores
                this.numReqBlockGenLackTransmitters++;
            } else if (request.getPair().getDestination().getRxs().isFullUtilized()) {// verificar
                // se
                // a
                // causa
                // do
                // bloqueio
                // foi
                // a
                // falta
                // de
                // receptores
                this.numReqBlockGenLackReceivers++;
            } else if (bloqueioPorFragmentacao(request.getCircuit())) { // verificar se a
                // causa do bloqueio
                // foi a
                // fragmentacao
                this.numReqBlockGeralFragment++;
            }

            // incrementar requisicoes bloqueadas por par
            i = this.numReqBlockPair
                    .get(request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName());
            if (i == null)
                i = 0;
            this.numReqBlockPair.put(
                    request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName(),
                    i + 1);
            // incrementar requisi��es bloqueadas por largura de banda
            i = this.numReqBlockBW.get(request.getRequiredBandwidth());
            if (i == null)
                i = 0;
            this.numReqBlockBW.put(request.getRequiredBandwidth(), i + 1);
            // incrementar requisi��es bloqueadas por par/larguraDeBanda
            HashMap<Double, Integer> bplb = this.numReqBlockPairBW
                    .get(request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName());
            if (bplb == null) {
                bplb = new HashMap<>();
                this.numReqBlockPairBW.put(
                        request.getPair().getSource().getName() + SEP + request.getPair().getDestination().getName(),
                        bplb);
            }
            i = bplb.get(request.getRequiredBandwidth());
            if (i == null)
                i = 0;
            bplb.put(request.getRequiredBandwidth(), i + 1);
        }
    }

    private boolean bloqueioPorFragmentacao(Circuit request) {
        if (request == null) return false;
        List<Link> links = request.getRoute().getLinkList();
        List<int[]> merge = links.get(0).getFreeSpectrumBands();
        int i;

        for (i = 1; i < links.size(); i++) {
            merge = IntersectionFreeSpectrum.merge(merge, links.get(i).getFreeSpectrumBands());
        }

        int totalLivre = 0;
        for (int[] faixa : merge) {
            totalLivre += (faixa[1] - faixa[0] + 1);
        }

        if (totalLivre > request.getModulation().requiredSlots(request.getRequiredBandwidth())) {
            return true;
        }

        return false;
    }

    /**
     * retorna a probabilidade de bloqueio geral na rede
     *
     * @return
     */
    public double getProbBlockGeral() {
        return ((double) this.numReqBlockGeral / (double) this.numReqGenGeral);
    }

    /**
     * retorna a probabilidade de bloqueio por fragmenta��o
     *
     * @return
     */
    public double getProbBlockFragGeral() {
        return ((double) this.numReqBlockGeralFragment / (double) this.numReqGenGeral);
    }

    /**
     * retorna a probabilidade de bloqueio por falta de transmissores
     *
     * @return
     */
    public double getProbBlockLackTxGen() {
        return ((double) this.numReqBlockGenLackTransmitters / (double) this.numReqGenGeral);
    }

    /**
     * retorna a probabilidade de bloqueio por falta de receptores
     *
     * @return
     */
    public double getProbBlockLackRxGen() {
        return ((double) this.numReqBlockGenLackReceivers / (double) this.numReqGenGeral);
    }

    /**
     * retorna a probabilidade de bloqueio de um determinado par
     *
     * @return
     */
    public double getProbBlockPair(Pair p) {
        double res;

        String or = p.getSource().getName();
        String dest = p.getDestination().getName();
        Integer gen = this.numReqGenPair.get(or + SEP + dest);
        if (gen == null)
            return 0; // nenhuma requisi��o gerada para este par

        Integer block = this.numReqBlockPair.get(or + SEP + dest);
        if (block == null)
            block = 0;

        res = ((double) block / (double) gen);

        return res;
    }

    /**
     * retorna a probabilidade de bloqueio de uma determinada largura de banda
     *
     * @param bw
     * @return
     */
    public double getProbBlockBandwidth(double bw) {
        double res;
        Integer gen = this.numReqGenBW.get(bw);
        if (gen == null)
            return 0; // nenhuma requisi��o gerada para este par

        Integer block = this.numReqBlockBW.get(bw);
        if (block == null)
            block = 0;

        res = ((double) block / (double) gen);

        return res;

    }

    /**
     * retorna a probabilidade de bloqueio de uma determinada largura de banda
     * em um determinado par
     *
     * @param bw
     * @return
     */
    public double getProbBlockPairBandwidth(Pair p, double bw) {
        double res;
        String or = p.getSource().getName();
        String dest = p.getDestination().getName();
        Integer gen = this.numReqGenPairBW.get(or + SEP + dest).get(bw);
        Integer block = 0;

        HashMap<Double, Integer> hashAux = this.numReqBlockPairBW.get(or + SEP + dest);
        if (hashAux == null || hashAux.get(bw) == null) {
            block = 0;
        } else {
            block = hashAux.get(bw);
        }

        res = ((double) block / (double) gen);

        return res;
    }

}

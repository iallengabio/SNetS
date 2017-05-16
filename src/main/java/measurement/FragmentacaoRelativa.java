package measurement;

import network.Circuit;
import network.Link;
import network.Mesh;
import util.CalculadorFragmentacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Esta classe representa a métrica de fragmentação relativa
 * A métrica representada por esta classe está associada a um ponto de carga e uma replicação
 *
 * @author Iallen
 */
public class FragmentacaoRelativa extends Measurement {

    public final static String SEP = "-";


    private HashMap<Integer, Double> fragmentacoesRelativas;
    private int numObservation;
    private Mesh mesh;


    public FragmentacaoRelativa(int loadPoint, int rep, Mesh mesh) {
        super(loadPoint, rep);
        this.mesh = mesh;
        fragmentacoesRelativas = new HashMap<>();
        numObservation = 0;
        //configurar as fragmentações relativas desejadas
        fragmentacoesRelativas.put(1, 0.0);
        fragmentacoesRelativas.put(2, 0.0);
        fragmentacoesRelativas.put(3, 0.0);
        fragmentacoesRelativas.put(5, 0.0);

    }


    /**
     * adiciona uma nova observação de fragmentação externa da rede
     *
     * @param request
     */
    public void addNewObservation(Circuit request) {
        this.observacaoAllLinks();
        numObservation++;
    }


    /**
     * faz uma observação da fragmentação relativa média em todos os links para cada valor de c configurado
     */
    private void observacaoAllLinks() {
        for (Integer c : fragmentacoesRelativas.keySet()) {
            this.observacaoAllLinks(c);
        }
    }


    /**
     * Faz uma observação da fragmentação relativa média em todos os links para o valor de c passado como parâmetro
     *
     * @param c
     */
    private void observacaoAllLinks(Integer c) {
        double fragMediaLink = 0.0;
        CalculadorFragmentacao cf = new CalculadorFragmentacao();
        for (Link link : mesh.getLinkList()) {
            double fAux = cf.fragmentacaoRelativa(link.getFreeSpectrumBands(), c);
            fragMediaLink += fAux;
        }
        fragMediaLink = fragMediaLink / ((double) mesh.getLinkList().size());

        double fAtual = this.fragmentacoesRelativas.get(c);
        fAtual += fragMediaLink;
        this.fragmentacoesRelativas.put(c, fAtual);
    }


    /**
     * retorna a lista de valores de C configurados para a realização de observações de fragmentação relativa
     *
     * @return
     */
    public List<Integer> getCList() {
        return new ArrayList<>(fragmentacoesRelativas.keySet());
    }

    public double getFragmentacaoRelativaMedia(int c) {
        return this.fragmentacoesRelativas.get(c) / ((double) this.numObservation);
    }


}

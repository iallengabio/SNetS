package measurement;

import network.Circuit;
import network.Link;
import network.Mesh;
import util.CalculadorFragmentacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Esta classe representa a m�trica de fragmenta��o relativa
 * A m�trica representada por esta classe est� associada a um ponto de carga e uma replica��o
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
        //configurar as fragmenta��es relativas desejadas
        fragmentacoesRelativas.put(1, 0.0);
        fragmentacoesRelativas.put(2, 0.0);
        fragmentacoesRelativas.put(3, 0.0);
        fragmentacoesRelativas.put(5, 0.0);

    }


    /**
     * adiciona uma nova observa��o de fragmenta��o externa da rede
     *
     * @param request
     */
    public void addNewObservation(Circuit request) {
        this.observacaoAllLinks();
        numObservation++;
    }


    /**
     * faz uma observa��o da fragmenta��o relativa m�dia em todos os links para cada valor de c configurado
     */
    private void observacaoAllLinks() {
        for (Integer c : fragmentacoesRelativas.keySet()) {
            this.observacaoAllLinks(c);
        }
    }


    /**
     * Faz uma observa��o da fragmenta��o relativa m�dia em todos os links para o valor de c passado como par�metro
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
     * retorna a lista de valores de C configurados para a realiza��o de observa��es de fragmenta��o relativa
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

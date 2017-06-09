package network;

import grmlsa.GRMLSA;
import grmlsa.Route;
import request.RequestForConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe que representa o plano de controle.
 * Esta classe deverï¿½ fazer as chamadas para os algoritmos RSA, armazenar rotas
 * em caso de roteamento fixo, prover informaï¿½ï¿½es a respeito do estado da rede, etc
 *
 * @author Iallen
 */
public class ControlPlane {

    //private static ControlPlane singleton;

    private Mesh mesh;

    private GRMLSA grmlsa;

    /**
     * a primeira chave representa o nó de origem.
     * a segunda chave representa o nó de destino.
     */
    private HashMap<String, HashMap<String, List<Circuit>>> circuitosAtivos;

    public ControlPlane() {
        circuitosAtivos = new HashMap<>();
    }

	/*
	/**
	 *
	 * mï¿½todo para acessar a ï¿½nica instï¿½ncia de ControlPlane existente (padrï¿½o singleton)
	 * @return
	 *
	public static ControlPlane getControlPlane(){
		if(singleton==null) singleton = new ControlPlane();
		
		return singleton;
	}
	*/


    /**
     * @param mesh the mesh to set
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        //inicializar a lista de circuitos ativos
        for (Node node1 : mesh.getNodeList()) {
            HashMap<String, List<Circuit>> hmAux = new HashMap<>();
            for (Node node2 : mesh.getNodeList()) {
                hmAux.put(node2.getName(), new ArrayList<Circuit>());
            }
            circuitosAtivos.put(node1.getName(), hmAux);
        }
    }


    /**
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * @param grmlsa the rsa to set
     */
    public void setRsa(GRMLSA grmlsa) {
        this.grmlsa = grmlsa;
    }


    /**
     * Este mï¿½todo tenta atender uma determinada requisiï¿½ï¿½o
     * alocando recursos caso disponï¿½vel e estabelecer o circuito
     *
     * @return
     */
    public boolean atenderRequisicao(RequestForConnection rfc) {

        return this.grmlsa.atenderRequisicao(rfc);

    }

    public void finalizarConexao(RequestForConnection rfc) {
        this.grmlsa.finalizarConexao(rfc);
    }

    /**
     * libera os recursos que estï¿½o sendo utilizados por um determinado circuito
     *
     * @param circuit
     */
    public void desalocarCircuito(Circuit circuit) {
        Route r = circuit.getRoute();

        liberarEspectro(circuit.getSpectrumAssigned(), r.getLinkList());

        //liberar tx e rx
        circuit.getSource().getTxs().freeTx();
        circuit.getDestination().getRxs().freeRx();

        circuitosAtivos.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).remove(circuit);

    }

    /**
     * Este metodo eh chamado apos a execucao dos algoritmos RSA para fazer a alocacao de recursos na rede
     *
     * @param request
     */
    private void allocarEspectro(Circuit request) {
        Route route = request.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[] = request.getSpectrumAssigned();
        alocarEspectro(chosen, links);
    }

    private boolean alocarEspectro(int chosen[], List<Link> links) {
        boolean notAbleAnymore = false;
        Link l;
        int i;
        for (i = 0; i < links.size(); i++) {
            l = links.get(i);
            notAbleAnymore = !l.useSpectrum(chosen);
            if (notAbleAnymore) break; //algum recurso nï¿½o estava mais disponï¿½vel,cancelar a alocaï¿½ï¿½o
        }


        return notAbleAnymore;
    }

    private void liberarEspectro(int chosen[], List<Link> links) {
        //liberar espectro
        for (Link link : links) {
            link.liberateSpectrum(chosen);
        }
    }


    /**
     * Este método tenta alocar um novo circuito na rede
     *
     * @param circuit
     * @return true caso o circuito tenha sido alocado com sucesso, false se o circuito não puder ser alocado.
     */
    public boolean allocarCircuito(Circuit circuit) {

        if (circuit.getSource().getTxs().alocTx()) {//conseguir alocar transmitter
            if (circuit.getDestination().getRxs().alocRx()) {//cconseguir alocar receiver
                if (this.grmlsa.criarNovoCircuito(circuit)) { //conseguir alocar espectro

                    //colocar a verificacao de QoT aqui

                    this.allocarEspectro(circuit);
                    circuitosAtivos.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).add(circuit);

                    return true;
                } else {//liberar transmiter e receiver
                    circuit.getSource().getTxs().freeTx();
                    circuit.getDestination().getRxs().freeRx();
                }
            } else {//liberar transmitter
                circuit.getSource().getTxs().freeTx();
            }
        }

        return false;

    }


    /**
     * aumentar a quantidade de slots usados por um determinado circuito
     *
     * @param circuit
     * @param faixaSup
     * @param faixaInf
     * @return
     */
    public boolean expandirCircuito(Circuit circuit, int faixaSup[], int faixaInf[]) {

        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        if (faixaSup != null) {
            chosen = faixaSup;
            alocarEspectro(chosen, links);
            specAssigAt[1] = faixaSup[1];
        }
        if (faixaInf != null) {
            chosen = faixaInf;
            alocarEspectro(chosen, links);
            specAssigAt[0] = faixaInf[0];
        }
        circuit.setSpectrumAssigned(specAssigAt);


        return true;
    }

    /**
     * reduz a quantidade de slots usados por um determinado circuito
     *
     * @param circuit
     * @param faixaInf
     * @param faixaSup
     * @return
     */
    public void retrairCircuito(Circuit circuit, int faixaInf[], int faixaSup[]) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        if (faixaInf != null) {
            chosen = faixaInf;
            liberarEspectro(chosen, links);
            specAssigAt[0] = faixaInf[1] + 1;
        }
        if (faixaSup != null) {
            chosen = faixaSup;
            liberarEspectro(chosen, links);
            specAssigAt[1] = faixaSup[0] - 1;
        }
        circuit.setSpectrumAssigned(specAssigAt);
    }

    /**
     * buscar circuitos ativos na rede com origem e destino especificados
     *
     * @param origem
     * @param destino
     * @return
     */
    public List<Circuit> procurarCircuitosAtivos(String origem, String destino) {
        return this.circuitosAtivos.get(origem).get(destino);
    }


}

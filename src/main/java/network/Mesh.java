package network;

import simulationControl.parsers.NetworkConfig;
import simulationControl.parsers.TrafficConfig;
import util.RandGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Mesh implements Serializable {

    private Vector<Node> nodeList;
    private Vector<Link> linkList;
    private Vector<Pair> pairList;
    private int guarBand;

    public Mesh(NetworkConfig nc, TrafficConfig tc) {
        this.guarBand = nc.getGuardBand();
        RandGenerator randGenerator = new RandGenerator();
        HashMap<String, Node> nodesAux = new HashMap<>();
        this.nodeList = new Vector<>();
        for (NetworkConfig.NodeConfig nodeConf : nc.getNodes()) {
            Node aux = new Node(nodeConf.getName(), nodeConf.getTransmiters(), nodeConf.getReceivers());
            this.nodeList.add(aux);
            nodesAux.put(aux.getName(), aux);
        }

        this.linkList = new Vector<>();
        for (NetworkConfig.LinkConfig linkConf : nc.getLinks()) {
            Link lAux = new Link(nodesAux.get(linkConf.getSource()).getOxc(), nodesAux.get(linkConf.getDestination()).getOxc(), linkConf.getSlots(), linkConf.getSectrum(), linkConf.getSize());
            linkList.add(lAux);
            nodesAux.get(linkConf.getSource()).getOxc().addLink(lAux);
        }

        //criar os pares
        this.pairList = new Vector<>();
        HashMap<String, HashMap<String, Pair>> pairsAux = new HashMap<>();
        for (Node src : this.nodeList) {
            pairsAux.put(src.getName(), new HashMap<>());
            for (Node dest : this.nodeList) {
                if(!src.equals(dest)) {
                    Pair pAux = new Pair(src, dest);
                    pairList.add(pAux);
                    pairsAux.get(src.getName()).put(dest.getName(), pAux);
                }
            }
        }

        //adicionar os geradores de requisição nos pares
        for (TrafficConfig.RequestGeneratorConfig rgc : tc.getRequestGenerators()) {
            Pair p = pairsAux.get(rgc.getSource()).get(rgc.getDestination());
            p.addRequestGenerator(new RequestGenerator(p, rgc.getBandwidth(), rgc.getHoldRate(), rgc.getArrivalRate(), rgc.getArrivalRateIncrease(), randGenerator));
        }


    }


    public Link getLink(String source, String destination) {
        for (int i = 0; i < linkList.size(); i++) {
            if ((linkList.get(i).getSource().getName() == source) &&
                    (linkList.get(i).getDestination().getName() == destination)) {
                return linkList.get(i);
            }

        }
        return null;
    }

    /**
     * retorna um Vector com todos os enlaces.
     *
     * @return Vector
     */
    public Vector<Link> getLinkList() {
        return linkList;
    }

    /**
     * computa todos os enlaces e armazena os enlaces em linkList.
     */
    private void coumputeAllLinks() {
        linkList = new Vector<Link>();
        for (int i = 0; i < nodeList.size(); i++) {
            linkList.addAll(nodeList.get(i).getOxc().getLinksList());
        }
    }


    /**
     * Getter for property nodeList.
     *
     * @return Vector with nodes.
     */
    public Vector<Node> getNodeList() {
        return nodeList;
    }


    //------------------------------------------------------------------------------

    /**
     * Localiza um Node em função do nome.
     *
     * @param name String
     * @return Node
     */
    public Node searchNode(String name) {
        for (int i = 0; i < this.nodeList.size(); i++) {
            Node tmp = nodeList.get(i);
            if (tmp.getName().equals(name)) {
                return tmp;
            }
        }
        return null;
    }


    /**
     * retorna os nós atingíveis a partir de um determinado nó
     *
     * @param n
     * @return
     */
    public List<Node> getAdjacents(Node n) {
        List<Node> res = new ArrayList<>();
        for (Oxc o : n.getOxc().getAllAdjacents()) {
            res.add(searchNode(o.getName()));
        }

        return res;
    }


    /**
     * @return the pairList
     */
    public Vector<Pair> getPairList() {
        return pairList;
    }


    public int getGuardBand() {
        return this.guarBand;
    }

}

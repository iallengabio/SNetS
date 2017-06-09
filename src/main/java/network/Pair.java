package network;

import simulationControl.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Pair implements Serializable {

    private Node source;
    private Node destination;
    private List<RequestGenerator> requestGenerators;

    /**
     * Cria instancia de Pair
     *
     * @param s Node
     * @param d Node
     */
    public Pair(Node s, Node d) {
        this.source = s;
        this.destination = d;
        requestGenerators = new ArrayList<RequestGenerator>();
    }


    //----------------------------------------------------------------------------

    /**
     * retorna o source
     *
     * @return Node
     */
    public Node getSource() {
        return this.source;
    }


//----------------------------------------------------------------------------

    /**
     * retorna o destino
     *
     * @return Node
     */
    public Node getDestination() {
        return this.destination;
    }


    //----------------------------------------------------------------------------

    /**
     * Retorna o nome do par
     *
     * @return String
     */
    public String getPairName() {
        return "(" + this.source.getName() + "," + this.destination.getName() + ")";
    }


    //----------------------------------------------------------------------------

    /**
     * Retorna o nome deste par:(o,d).
     *
     * @return Object
     */
    public String getName() {
        return ("(" + this.source.getName() + "," + this.destination.getName() +
                ")");
    }


    /**
     * adiciona um gerador de requisiçõe ao par
     *
     * @param rg
     */
    public void addRequestGenerator(RequestGenerator rg) {
        requestGenerators.add(rg);
        Util.bandwidths.add(rg.getBandwidth()); //utilizado na hora de gravar em arquivo os resultados da simulação
    }

    /**
     * @return the requestGenerators
     */
    public List<RequestGenerator> getRequestGenerators() {
        return requestGenerators;
    }

    @Override
    public boolean equals(Object o) {
        try {
            System.out.println("ei");
            Pair p = (Pair) o;
            return (this.getSource().equals(p.source) && this.getDestination().equals(p.getDestination()));

        } catch (Exception ex) {
            return false;
        }
    }


}

package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import simulationControl.Util;

/**
 * This class represents a pair of source (s) and destination (d) nodes
 * 
 * @author Iallen
 */
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
    
    /**
     * Returns the source node
     *
     * @return Node
     */
    public Node getSource() {
        return this.source;
    }

    /**
     * Returns the destination node
     *
     * @return Node
     */
    public Node getDestination() {
        return this.destination;
    }

    /**
     * Returns the name of the pair (s, d)
     *
     * @return String
     */
    public String getPairName() {
        return "(" + this.source.getName() + "," + this.destination.getName() + ")";
    }

    /**
     * Returns the pair name
     * 
     * @return String
     */
    public String getName() {
        return ("(" + this.source.getName() + "," + this.destination.getName() + ")");
    }
    
    /**
     * Add a request generator to the pair
     *
     * @param rg RequestGenerator
     */
    public void addRequestGenerator(RequestGenerator rg) {
        requestGenerators.add(rg);
    }

    /**
     * Returns the request generators
     * 
     * @return List<RequestGenerator> the requestGenerators
     */
    public List<RequestGenerator> getRequestGenerators() {
        return requestGenerators;
    }

    /**
     * Checks whether a given pair is equal to this pair
     * 
     * @param o Object
     */
    @Override
    public boolean equals(Object o) {
        try {
            Pair p = (Pair) o;
            return (this.getSource().equals(p.source) && this.getDestination().equals(p.getDestination()));

        } catch (Exception ex) {
            return false;
        }
    }

    public String toString(){
        return source.getName() + "-" + destination.getName();
    }

}

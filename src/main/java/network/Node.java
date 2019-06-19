package network;

import java.io.Serializable;
import java.util.Vector;

/**
 * This class represents a network topology node
 * 
 * @author Iallen
 */
public class Node implements Serializable {

    private String name;
    private Oxc oxc;
    private Vector<Pair> pairs;
    private Transmitters txs;
    private Receivers rxs;
    
    private Regenerators regenerators;
    
    /**
     * Creates a new instance of Node.
     *
     * @param name String
     */
    @Deprecated
    public Node(String name) {
        this.name = name;
        this.oxc = new Oxc(name);
        this.txs = new Transmitters();
        this.rxs = new Receivers();
    }

    /**
     * Creates a new instance of Node
     * 
     * @param name String
     * @param numTx int
     * @param numRx int
     */
    public Node(String name, int numTx, int numRx, int numRegenerators, int maxAmplitudeBVT) {
        this.name = name;
        this.oxc = new Oxc(name);
        this.txs = new Transmitters(numTx,maxAmplitudeBVT);
        this.rxs = new Receivers(numRx);
        this.regenerators = new Regenerators(numRegenerators);
    }

    /**
     * Checks whether a given node is equal to this node
     * 
     * @param o Object
     */
    public boolean equals(Object o) {
        if (o instanceof Node) {
            if (name.equals(((Node) o).getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for property oxc
     *
     * @return Oxc oxc this Node.
     */
    public Oxc getOxc() {
        return this.oxc;
    }

    /**
     * Getter for property name
     *
     * @return String name name this Node.
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the pairs whose origin is this node.
     *
     * @return Vector<Pair>
     */
    public Vector<Pair> getPairs() {
        return pairs;
    }

    /**
     * Returns the bank of transmitters of this node
     *
     * @return Transmitters
     */
    public Transmitters getTxs() {
        return txs;
    }

    /**
     * Returns the bank of receivers of this node
     *
     * @return Receivers
     */
    public Receivers getRxs() {
        return rxs;
    }


    /**
     * Configures the pairs whose origin and this node
     *
     * @param pairs Vector<Pair>
     */
    public void setPairs(Vector<Pair> pairs) {
        this.pairs = pairs;
    }

    
    /**
     * Returns the bank of regenerators
     * 
     * @return regenerators Regenerators
     */
    public Regenerators getRegenerators(){
    	return regenerators;
    }

    public String toString(){
        return "node: " + name;
    }

}

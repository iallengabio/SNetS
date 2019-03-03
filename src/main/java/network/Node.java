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
    private int trafficWeigth;
    private Vector<Pair> pairs;
    private Transmitters txs;
    private Receivers rxs;
    private double numberReqGenerated;
    private double distanceToCentralNodeControlPlane = -9999999;
    
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
        this.trafficWeigth = 1;
        this.txs = new Transmitters();
        this.rxs = new Receivers();
        this.numberReqGenerated = 0;
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
        this.trafficWeigth = 1;
        this.txs = new Transmitters(numTx,maxAmplitudeBVT);
        this.rxs = new Receivers(numRx);
        this.regenerators = new Regenerators(numRegenerators);
        this.numberReqGenerated = 0;
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
     * Returns the weight of the node
     *
     * @return int
     */
    public int getWeigth() {
        return this.trafficWeigth;
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
     * Returns the number of requests generated with this node
     *
     * @return double
     */
    public double getNumberReqGenerated() {
        return numberReqGenerated;
    }

    /**
     * Returns the distance from this node to the Central Node of the Control Plane
     *
     * @return double
     */
    public double getDistanceToCentralNodeControlPlane() {
        return distanceToCentralNodeControlPlane;
    }

    /**
     * Sets the weight of the node
     *
     * @param weigth int
     */
    public void setTrafficWeigth(int weigth) {
        this.trafficWeigth = weigth;
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
     * Configures the distance from this node to the Central Node of the Control Plane
     *
     * @param distanceToCentralNodeControlPlane double
     */
    public void setDistanceToCentralNodeControlPlane(double distanceToCentralNodeControlPlane) {
        this.distanceToCentralNodeControlPlane = distanceToCentralNodeControlPlane;
    }

    /**
     * Increases the number of requests generated with this node
     */
    public void incNumberReqGenerated() {
        this.numberReqGenerated++;
    }

    /**
     * finish
     *
     * @param timeHours double
     */
    public void finish(double timeHours) {
        // this.reqQueue.finish(timeHours);
    }

    /**
     * restart
     */
    public void reStart() {
        this.txs.reStart();
        this.rxs.reStart();
    }
    
    /**
     * Returns the bank of regenerators
     * 
     * @return regenerators Regenerators
     */
    public Regenerators getRegenerators(){
    	return regenerators;
    }

}

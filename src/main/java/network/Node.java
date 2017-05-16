package network;

import java.io.Serializable;
import java.util.Vector;


public class Node implements Serializable {

    private String name;
    private Oxc oxc;
    private int trafficWeigth;
    private Vector<Pair> pairs;
    private Transmitters txs;
    private Receivers rxs;
    private double numberReqGenerated;
    private double distanceToCentralNodeControlPlane = -9999999;

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

    public Node(String name, int numTx, int numRx) {
        this.name = name;
        this.oxc = new Oxc(name);
        this.trafficWeigth = 1;
        this.txs = new Transmitters(numTx);
        this.rxs = new Receivers(numRx);
        this.numberReqGenerated = 0;
    }

    public boolean equals(Object o) {
        if (o instanceof Node) {
            if (name.equals(((Node) o).getName())) {
                return true;
            }
        }
        return false;
    }


//------------------------------------------------------------------------------

    /**
     * Getter for property oxc
     *
     * @return Oxc oxc this Node.
     */
    public Oxc getOxc() {
        return this.oxc;
    }

//------------------------------------------------------------------------------

    /**
     * Getter for property name
     *
     * @return String name name this Node.
     */
    public String getName() {
        return name;
    }

//------------------------------------------------------------------------------

    /**
     * Retorna o privilegio do nó
     *
     * @return int
     */
    public int getWeigth() {
        return this.trafficWeigth;
    }

    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------

    /**
     * Retorna os pares cuja origem e este no.
     *
     * @return Vector
     */
    public Vector<Pair> getPairs() {
        return pairs;
    }


    //------------------------------------------------------------------------------

    /**
     * Retorna o banco de transmissores deste No
     *
     * @return Transmitters
     */
    public Transmitters getTxs() {
        return txs;
    }

    //------------------------------------------------------------------------------

    /**
     * Retorna o banco de Receptores deste No
     *
     * @return Receivers
     */
    public Receivers getRxs() {
        return rxs;
    }

    //------------------------------------------------------------------------------

    /**
     * Retorna o numero de requisicoes geradas com origem neste No
     *
     * @return double
     */
    public double getNumberReqGenerated() {
        return numberReqGenerated;
    }

    //------------------------------------------------------------------------------

    /**
     * Retorna a distancia para deste no para o No Central do Plano de Controle
     *
     * @return double
     */
    public double getDistanceToCentralNodeControlPlane() {
        return distanceToCentralNodeControlPlane;
    }

    //------------------------------------------------------------------------------

    /**
     * configura o peso do nó
     *
     * @param weigth int
     */
    public void setTrafficWeigth(int weigth) {
        this.trafficWeigth = weigth;
    }


    //------------------------------------------------------------------------------

    /**
     * Configura os pares cuja origem e este no
     *
     * @param pairs Vector
     */
    public void setPairs(Vector<Pair> pairs) {
        this.pairs = pairs;
    }


    //------------------------------------------------------------------------------

    /**
     * Configura a distancia para deste no para o No Central do Plano de Controle
     *
     * @param distanceToCentralNodeControlPlane double
     */
    public void setDistanceToCentralNodeControlPlane(double distanceToCentralNodeControlPlane) {
        this.distanceToCentralNodeControlPlane = distanceToCentralNodeControlPlane;

    }

    //------------------------------------------------------------------------------

    /**
     * Incrementa o numero de requisicoes geradas com origem neste No
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

    //restart
    public void reStart() {

        this.txs.reStart();
        this.rxs.reStart();


    }

}

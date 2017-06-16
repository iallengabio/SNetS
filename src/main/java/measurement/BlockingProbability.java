package measurement;

import java.util.HashMap;

import network.ControlPlane;
import network.Pair;
import request.RequestForConnection;
import simulationControl.resultManagers.BlockingProbResultManager;

/**
 * This class represents the locking probability metric (general, per pair, per bandwidth, 
 * per pair / BandWidth)
 * The metric represented by this class is associated with a load point and a replication
 *
 * @author Iallen
 */
public class BlockingProbability extends Measurement {

    public final static String SEP = "-";

    // Overall blockage probability
    private int numGeneralGeneratedReq;
    private int numGeneralRegBlockProb;
    private int numReqBlockByFragmentation;
    private int numReqBlockByLackTransmitters;
    private int numReqBlockByLackReceivers;
    private int numRegBlockByQoTN;
    private int numRegBlockByQoTO;
    private int numRegBlockByOther;

    // Blocking probability per pair
    private HashMap<String, Integer> numReqGenPair;
    private HashMap<String, Integer> numReqBlockPair;

    // Blocking probability per bandwidth
    private HashMap<Double, Integer> numReqGenBW;
    private HashMap<Double, Integer> numReqBlockBW;

    // Blocking probability per pair / BandWidth
    private HashMap<String, HashMap<Double, Integer>> numReqGenPairBW;
    private HashMap<String, HashMap<Double, Integer>> numReqBlockPairBW;

    /**
     * Creates a new instance of BlockingProbability
     * 
     * @param loadPoint int
     * @param rep int
     */
    public BlockingProbability(int loadPoint, int rep) {
        super(loadPoint, rep);

        this.numGeneralGeneratedReq = 0;
        this.numGeneralRegBlockProb = 0;
        this.numReqBlockByFragmentation = 0;
        this.numReqBlockByLackTransmitters = 0;
        this.numReqBlockByLackReceivers = 0;
        this.numRegBlockByQoTN = 0;
        this.numRegBlockByQoTO = 0;
        this.numRegBlockByOther = 0;

        this.numReqGenBW = new HashMap<>();
        this.numReqBlockBW = new HashMap<>();
        this.numReqGenPair = new HashMap<>();
        this.numReqBlockPair = new HashMap<>();
        this.numReqGenPairBW = new HashMap<>();
        this.numReqBlockPairBW = new HashMap<>();
        
        fileName = "_BlockingProbability.csv";
		resultManager = new BlockingProbResultManager();
    }

    /**
     * Adds a new observation of block or not a request
     *
     * @param cp ControlPlane
     * @param success boolean
     * @param request RequestForConnection
     */
    public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request) {
    	
    	StringBuilder sbPair = new StringBuilder();
		sbPair.append(request.getPair().getSource().getName());
		sbPair.append(SEP);
		sbPair.append(request.getPair().getDestination().getName());
		String pairName = sbPair.toString();
    	
        // Increment generated general requisitions
        this.numGeneralGeneratedReq++;

        // Increment requests generated by pair
        Integer i = this.numReqGenPair.get(pairName);
        if (i == null) i = 0;
        this.numReqGenPair.put(pairName, i + 1);

        // Increment requests generated per bandwidth
        i = this.numReqGenBW.get(request.getRequiredBandwidth());
        if (i == null) i = 0;
        this.numReqGenBW.put(request.getRequiredBandwidth(), i + 1);

        // Increment requests generated by pair / BandWidth
        HashMap<Double, Integer> gplb = this.numReqGenPairBW.get(pairName);
        if (gplb == null) {
            gplb = new HashMap<>();
            this.numReqGenPairBW.put(pairName, gplb);
        }
        i = gplb.get(request.getRequiredBandwidth());
        if (i == null) i = 0;
        gplb.put(request.getRequiredBandwidth(), i + 1);

        // If there is a lock
        if (!success) {
            // Increment blocked requests general
            this.numGeneralRegBlockProb++;

            if (request.getPair().getSource().getTxs().isFullUtilized()) { // Check whether the cause of the block was the lack of transmitters
                this.numReqBlockByLackTransmitters++;
                
            } else if (request.getPair().getDestination().getRxs().isFullUtilized()) { // Check whether the cause of the block was the lack receivers
                this.numReqBlockByLackReceivers++;
                
            } else if (cp.isBlockingByQoTN(request.getCircuit())){ // Check whether the cause of the block was the QoTN
            	this.numRegBlockByQoTN++;
            	
            } else if (cp.isBlockingByQoTO(request.getCircuit())) { // Check whether the cause of the block was the QoTO
            	this.numRegBlockByQoTO++;
            	
            } else if (cp.isBlockingByFragmentation(request.getCircuit())) { // Check whether the cause of the block was the fragmentation
                this.numReqBlockByFragmentation++;
                
            } else { // Blocking occurred due to lack of free slots
            	this.numRegBlockByOther++;
            }

            // Increase requests blocked by pair
            i = this.numReqBlockPair.get(pairName);
            if (i == null) i = 0;
            this.numReqBlockPair.put(pairName, i + 1);
            
            // Increase requests blocked by bandwidth
            i = this.numReqBlockBW.get(request.getRequiredBandwidth());
            if (i == null) i = 0;
            this.numReqBlockBW.put(request.getRequiredBandwidth(), i + 1);
            
            // Increase request blocked by pair / bandwidth
            HashMap<Double, Integer> bplb = this.numReqBlockPairBW.get(pairName);
            if (bplb == null) {
                bplb = new HashMap<>();
                this.numReqBlockPairBW.put(pairName,bplb);
            }
            i = bplb.get(request.getRequiredBandwidth());
            if (i == null) i = 0;
            bplb.put(request.getRequiredBandwidth(), i + 1);
        }
    }

    /**
     * Returns the overall blocking probability on the network
     *
     * @return double
     */
    public double getGeneralBlockProb() {
        return ((double) this.numGeneralRegBlockProb / (double) this.numGeneralGeneratedReq);
    }

    /**
     * Returns the blocking probability due to fragmentation
     *
     * @return double
     */
    public double getBlockProbByFragmentation() {
        return ((double) this.numReqBlockByFragmentation / (double) this.numGeneralGeneratedReq);
    }

    /**
     * Returns the blocking probability due to lack of transmitters
     *
     * @return double
     */
    public double getReqBlockByLackTx() {
        return ((double) this.numReqBlockByLackTransmitters / (double) this.numGeneralGeneratedReq);
    }

    /**
     * Returns the blocking probability due to lack of receivers
     *
     * @return double
     */
    public double getReqBlockByLackRx() {
        return ((double) this.numReqBlockByLackReceivers / (double) this.numGeneralGeneratedReq);
    }
    
    
    /**
     * Returns the probability of blocking by QoTN
     *
     * @return double
     */
    public double getRegBlockByQoTN() {
        return ((double) this.numRegBlockByQoTN / (double) this.numGeneralGeneratedReq);
    }
    
    /**
     * Returns the probability of blocking by QoTO
     *
     * @return double
     */
    public double getRegBlockByQoTO() {
        return ((double) this.numRegBlockByQoTO / (double) this.numGeneralGeneratedReq);
    }
    
    /**
     * Returns the probability of blocking by other
     *
     * @return double
     */
    public double getRegBlockByOther() {
        return ((double) this.numRegBlockByOther / (double) this.numGeneralGeneratedReq);
    }
    
    /**
     * Returns the probability of blocking a given pair
     *
     * @return double
     */
    public double getProbBlockPair(Pair p) {
        double res;

        String source = p.getSource().getName();
        String destination = p.getDestination().getName();
        Integer gen = this.numReqGenPair.get(source + SEP + destination);
        if (gen == null)
            return 0; // No requests generated for this pair

        Integer block = this.numReqBlockPair.get(source + SEP + destination);
        if (block == null)
            block = 0;

        res = ((double) block / (double) gen);

        return res;
    }

    /**
     * Returns the blocking probability of a given bandwidth
     *
     * @param bw double
     * @return double
     */
    public double getProbBlockBandwidth(double bw) {
        double res;
        Integer gen = this.numReqGenBW.get(bw);
        if (gen == null)
            return 0; // No requests generated for this pair

        Integer block = this.numReqBlockBW.get(bw);
        if (block == null)
            block = 0;

        res = ((double) block / (double) gen);

        return res;
    }

    /**
     * Returns the blocking probability of a given bandwidth in a given pair
     *
     * @param bw double
     * @return double
     */
    public double getProbBlockPairBandwidth(Pair p, double bw) {
        double res;
        String source = p.getSource().getName();
        String destination = p.getDestination().getName();
        
        Integer gen = null;
        if(this.numReqGenPairBW.get(source + SEP + destination) != null){
        	gen = this.numReqGenPairBW.get(source + SEP + destination).get(bw);
        }
        
        if(gen == null) return 0; // No requests generated for this pair and bandwidth
        Integer block = 0;

        HashMap<Double, Integer> hashAux = this.numReqBlockPairBW.get(source + SEP + destination);
        if (hashAux == null || hashAux.get(bw) == null) {
            block = 0;
        } else {
            block = hashAux.get(bw);
        }

        res = ((double) block / (double) gen);

        return res;
    }

}

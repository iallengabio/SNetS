package measurement;

import java.util.HashMap;
import java.util.List;

import network.Circuit;
import network.ControlPlane;
import network.Pair;
import request.RequestForConnection;
import simulationControl.Util;
import simulationControl.parsers.SimulationRequest;
import simulationControl.resultManagers.BandwidthBlockingProbResultManager;

/**
 * This class represents the bandwidth blocking probability metric (general, per pair, per bandwidth, 
 * per pair / BandWidth)
 * The metric represented by this class is associated with a load point and a replication
 * 
 * @author Iallen
 */
public class BandwidthBlockingProbability extends Measurement{
	
	public final static String SEP = "-";
	
	// Overall blockage probability
	private double generalRequestedBandwidth;
	private double generalBandwidthBlockingProbability;
	private double bandwidthBlockingByFragmentation;
    private double bandwidthBlockingByLackTransmitters;
    private double bandwidthBlockingByLackReceivers;
    private double bandwidthBlockingByQoTN;
    private double bandwidthBlockingByQoTO;
    private double bandwidthBlockingByOther;
	
	// Blocking probability per pair
	private HashMap<String, Double> requestedBandwidthPerPair;
	private HashMap<String, Double> bandwidthBlockedPerPair;
	
	// Blocking probability per bandwidth
	private HashMap<Double, Double> requestedBandwidthPerBW;
	private HashMap<Double, Double> bandwidthBlockedPerBW;
	
	// Blocking probability per pair / BandWidth
	private HashMap<String, HashMap<Double,Double>> requestedBandwidthPairBW;
	private HashMap<String, HashMap<Double,Double>> bandwidthBlockedPairBW;
	private Util util;

	/**
	 * Creates a new instance of BandwidthBlockingProbability
	 * 
	 * @param loadPoint int
	 * @param rep int
	 */
	public BandwidthBlockingProbability(int loadPoint, int rep, Util util){
		super(loadPoint, rep);
		this.util = util;
		// bandwidth blocking probability general
		this.generalRequestedBandwidth = 0.0;
		this.generalBandwidthBlockingProbability = 0.0;

		this.requestedBandwidthPerBW = new HashMap<>();
		this.bandwidthBlockedPerBW = new HashMap<>();
		this.requestedBandwidthPerPair = new HashMap<>();
		this.bandwidthBlockedPerPair = new HashMap<>();
		this.requestedBandwidthPairBW = new HashMap<>();
		this.bandwidthBlockedPairBW = new HashMap<>();
		resultManager = new BandwidthBlockingProbResultManager();
	}
	
	/**
	 * Adds a new observation of block or not a request
	 * 
	 * @param cp ControlPlane
	 * @param success boolean
	 * @param request RequestForConnection
	 */
	public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request){
		// Calculate the amount of band requested by the circuit
		Double time = request.getTimeOfFinalizeHours() - request.getTimeOfRequestHours();
		Double bandwidth = time * request.getRequiredBandwidth();
		
		StringBuilder sbPair = new StringBuilder();
		sbPair.append(request.getPair().getSource().getName());
		sbPair.append(SEP);
		sbPair.append(request.getPair().getDestination().getName());
		String pairName = sbPair.toString();
		
		// Increment generated general requisitions
		this.generalRequestedBandwidth += bandwidth;			
		
		// Increment requests generated by pair
		Double i = this.requestedBandwidthPerPair.get(pairName);
		if(i==null) i=0.0;
		this.requestedBandwidthPerPair.put(pairName, i+bandwidth);			
		
		// Increment requests generated by bandwidth
		i = this.requestedBandwidthPerBW.get(request.getRequiredBandwidth());
		if(i==null) i=0.0;
		this.requestedBandwidthPerBW.put(request.getRequiredBandwidth(), i+bandwidth);			
		
		// Increment requests generated by pair / bandwidth
		HashMap<Double,Double> gplb = this.requestedBandwidthPairBW.get(pairName);
		if(gplb==null){
			gplb = new HashMap<>();
			this.requestedBandwidthPairBW.put(pairName, gplb);
		}
		i = gplb.get(request.getRequiredBandwidth());
		if(i==null) i=0.0;
		gplb.put(request.getRequiredBandwidth(), i+bandwidth);
		
		// If there is a lock
		if(!success){
			// Increment blocked requests general
			this.generalBandwidthBlockingProbability += bandwidth;
			
			for (Circuit c: request.getCircuits()) {
				if(c.isWasBlocked()){//considers that only one circuit has been blocked
					switch(c.getBlockCause()){
						case Circuit.BY_LACK_TX:
							this.bandwidthBlockingByLackTransmitters += bandwidth;
							break;
						case Circuit.BY_LACK_RX:
							this.bandwidthBlockingByLackReceivers += bandwidth;
							break;
						case Circuit.BY_QOTN:
							this.bandwidthBlockingByQoTN += bandwidth;
							break;
						case Circuit.BY_QOTO:
							this.bandwidthBlockingByQoTO += bandwidth;
							break;
						case Circuit.BY_FRAGMENTATION:
							this.bandwidthBlockingByFragmentation += bandwidth;
							break;
						case Circuit.BY_OTHER:
							this.bandwidthBlockingByOther += bandwidth;
							break;
					}
					break;
				}
			}
			
			// Increment blocked requests per pair
			i = this.bandwidthBlockedPerPair.get(pairName);
			if(i==null) i=0.0;
			this.bandwidthBlockedPerPair.put(pairName, i+bandwidth);			
			
			// Increment blocked requests per bandwidth
			i = this.bandwidthBlockedPerBW.get(request.getRequiredBandwidth());
			if(i==null) i=0.0;
			this.bandwidthBlockedPerBW.put(request.getRequiredBandwidth(), i+bandwidth);			
			
			// Increment blocked request per pair / bandwidth
			HashMap<Double,Double> bplb = this.bandwidthBlockedPairBW.get(pairName);
			if(bplb==null){
				bplb = new HashMap<>();
				this.bandwidthBlockedPairBW.put(pairName, bplb);
			}
			i = bplb.get(request.getRequiredBandwidth());
			if(i==null) i=0.0;
			bplb.put(request.getRequiredBandwidth(), i+bandwidth);
		}
	}

	@Override
	public String getFileName() {
		return SimulationRequest.Result.FILE_BANDWIDTH_BLOCKING_PROBABILITY;
	}

	/**
	 * Returns the probability of blocking the general bandwidth on the network
	 * 
	 * @return double
	 */
	public double getProbBlockGeneral(){
		return (this.generalBandwidthBlockingProbability / this.generalRequestedBandwidth);
	}

	/**
	 * Returns total requested bandwidth.
	 * @return
	 */
	public double getGeneralRequestedBandwidth() {
		return generalRequestedBandwidth;
	}
	/**
     * Returns the blocking probability due to fragmentation
     *
     * @return double
     */
    public double getBandwidthBlockingByFragmentation() {
        return ( this.bandwidthBlockingByFragmentation /  this.generalRequestedBandwidth);
    }

    /**
     * Returns the blocking probability due to lack of transmitters
     *
     * @return double
     */
    public double getBandwidthBlockingByLackTx() {
        return (this.bandwidthBlockingByLackTransmitters / this.generalRequestedBandwidth);
    }

    /**
     * Returns the blocking probability due to lack of receivers
     *
     * @return double
     */
    public double getBandwidthBlockingByLackRx() {
        return (this.bandwidthBlockingByLackReceivers / this.generalRequestedBandwidth);
    }
    
    
    /**
     * Returns the probability of blocking by QoTN
     *
     * @return double
     */
    public double getBandwidthBlockingByQoTN() {
        return (this.bandwidthBlockingByQoTN / this.generalRequestedBandwidth);
    }
    
    /**
     * Returns the probability of blocking by QoTO
     *
     * @return double
     */
    public double getBandwidthBlockingByQoTO() {
        return (this.bandwidthBlockingByQoTO / this.generalRequestedBandwidth);
    }
    
    /**
     * Returns the probability of blocking by other
     *
     * @return double
     */
    public double getBandwidthBlockingByOther() {
        return (this.bandwidthBlockingByOther / this.generalRequestedBandwidth);
    }

	/**
	 * Returns the bandwidth blocking probability of a given pair
	 * 
	 * @param p Pair
	 * @return double
	 */
	public double getProbBlockPair(Pair p){
		double res;
		
		String source = p.getSource().getName();
		String destination = p.getDestination().getName();
		Double gen = this.requestedBandwidthPerPair.get(source + SEP + destination);
		if(gen==null) return 0; // No requests generated for this pair
		
		Double block = this.bandwidthBlockedPerPair.get(source + SEP + destination);
		if(block==null) block = 0.0;
		
		res = ((double) block / (double) gen);		
		
		return res;
	}

	/**
	 * Returns the bandwidth blocking probability of a given bandwidth
	 * 
	 * @param bw double
	 * @return double
	 */
	public double getProbBlockBandwidth(double bw){
		double res;
		Double gen = this.requestedBandwidthPerBW.get(bw);
		if(gen==null) return 0; // No requests generated for this pair
		
		Double block = this.bandwidthBlockedPerBW.get(bw);
		if(block==null) block = 0.0;
		
		res = ((double) block / (double) gen);		
		
		return res;
	}
	
	/**
	 * Returns the bandwidth blocking probability of a given bandwidth in a given pair
	 * 
	 * @param p Pair
	 * @param bw double
	 * @return double
	 */
	public double getProbBlockPairBandwidth(Pair p, double bw){
		double res;
		String or = p.getSource().getName();
		String dest = p.getDestination().getName();
		
		Double gen = null;
		if(this.requestedBandwidthPairBW.get(or + SEP + dest) != null){
			gen = this.requestedBandwidthPairBW.get(or + SEP + dest).get(bw);
		}
		
		if(gen == null) return 0; // No requests generated for this pair
		Double block = 0.0;
		
		HashMap<Double, Double> hashAux = this.bandwidthBlockedPairBW.get(or + SEP + dest);
		if(hashAux==null || hashAux.get(bw)==null){
			block = 0.0;
		}else{
			block = hashAux.get(bw);
		}
		
		res = ((double) block / (double) gen);		
		
		return res;
	}

	public Util getUtil() {
		return this.util;
	}

}

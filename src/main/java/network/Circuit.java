package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import request.RequestForConnection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an established transparent circuit in an optical network
 * 
 * @author Iallen
 */
public class Circuit implements Comparable<Object>, Serializable {

	public static final int BY_LACK_TX = 1;
	public static final int BY_LACK_RX = 2;
	public static final int BY_FRAGMENTATION = 3;
	public static final int BY_QOTN = 4;
	public static final int BY_QOTO = 5;
	public static final int BY_OTHER = 6;
	
	protected static int quantity = 0;
    protected Integer id;
    protected Pair pair;
    protected Route route;
    protected int spectrumAssigned[];
    protected Modulation modulation;
    protected List<RequestForConnection> requests; // Requests attended by this circuit
    
    protected double SNR; //dB
	protected boolean QoT;
	protected boolean QoTForOther;
	protected double powerConsumption;

	protected boolean wasBlocked = false;
	protected int blockCause;
	
	protected double launchPowerLinear;
	protected int guardBand;

    /**
     * Instantiates a circuit with the list of requests answered by it in empty
     */
    public Circuit() {
    	this.id = quantity++;
        this.requests = new ArrayList<>();
        
        this.QoT = true; //Assuming that a request always starts with admissible QoT
        this.QoTForOther = true; //Assuming that it is admissible for the other requirements
        
        this.launchPowerLinear = Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the source and destination pair of the circuit
     * 
     * @return the pair - Pair
     */
    public Pair getPair() {
        return pair;
    }

    /**
     * Sets the source and destination pair of the circuit
     * 
     * @param pair the pair to set
     */
    public void setPair(Pair pair) {
        this.pair = pair;
    }

    /**
     * Returns the route used by the circuit
     * 
     * @return the route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Configures the route used by the
     * 
     * @param route the route to set
     */
    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * Returns the total bandwidth used by the circuit
     * 
     * @return the requiredBandwidth
     */
    public double getRequiredBandwidth() {
        double res = 0.0;

        for (RequestForConnection r : requests) {
            res += r.getRequiredBandwidth();
        }

        return res;
    }

	/**
	 * Compute the residual capacity of circuit in terms of bandwidth
	 * @return
	 */
	public double getResidualCapacity(){
    	double rb = getRequiredBandwidth();
    	double cap = getModulation().potentialBandwidth(spectrumAssigned[1] - spectrumAssigned[0] + 1);
		return cap - rb;
	}

    /**
     * Returns the source node of the circuit
     * 
     * @return
     */
    public Node getSource() {
        return pair.getSource();
    }

    /**
     * Returns the destination node of the circuit
     * 
     * @return Node
     */
    public Node getDestination() {
        return pair.getDestination();
    }

    /**
     * Returns the spectrum band allocated by the circuit
     * 
     * @return int[]
     */
    public int[] getSpectrumAssigned() {
        return spectrumAssigned;
    }

    /**
     * Configures the spectrum band allocated by the circuit
     * 
     * @param sa int[]
     */
    public void setSpectrumAssigned(int sa[]){
    	if(sa != null && sa[0] > sa[1]){
    		throw new UnsupportedOperationException();
		}
        spectrumAssigned = sa;
    }

    /**
     * Returns the modulation format used by the circuit
     * 
     * @return the modulation - Modulation
     */
    public Modulation getModulation() {
        return modulation;
    }

    /**
     * Configures the modulation format used by the circuit
     * 
     * @param modulation the modulation to set - Modulation
     */
    public void setModulation(Modulation modulation) {
        this.modulation = modulation;
        setGuardBand(modulation.getGuardBand());
    }

    /**
     * Adds a given request to the list of requests answered by the circuit
     * 
     * @param rfc RequestForConnection
     */
    public void addRequest(RequestForConnection rfc) {
        requests.add(rfc);
    }

    /**
     * Removes a given request from the list of requests served by the circuit
     * 
     * @param rfc
     */
    public void removeRequest(RequestForConnection rfc) {
        requests.remove(rfc);
    }

    /**
     * Returns the list of requests answered by the circuit
     * 
     * @return List<RequestForConnection>
     */
    public List<RequestForConnection> getRequests() {
        return requests;
    }
    
    /**
     * Returns the SNR
     * 
	 * @return the SNR double
	 */
	public double getSNR() {
		return SNR;
	}

	/**
	 * Sets the SNR
	 * 
	 * @param SNR the SNR to set double
	 */
	public void setSNR(double SNR) {
		this.SNR = SNR;
	}

	/**
	 * Returns the QoT
	 * 
	 * @return the QoT boolean
	 */
	public boolean isQoT() {
		return QoT;
	}

	/**
	 * Sets the QoT
	 * 
	 * @param QoT the QoT to set boolean
	 */
	public void setQoT(boolean QoT) {
		this.QoT = QoT;
	}

	/**
	 * Return if circuit is blocked
	 * 
	 * @return boolean
	 */
	public boolean isWasBlocked() {
		return wasBlocked;
	}

	/**
	 * Sets if circuit is blocked
	 * 
	 * @param wasBlocked
	 */
	public void setWasBlocked(boolean wasBlocked) {
		this.wasBlocked = wasBlocked;
	}
	
	/**
	 * Returns the type of blockage suffered by the circuit
	 * 
	 * @return int
	 */
	public int getBlockCause() {
		return blockCause;
	}

	/**
	 * Sets the type of blockage suffered by the circuit
	 * 
	 * @param blockCause int
	 */
	public void setBlockCause(int blockCause) {
		this.blockCause = blockCause;
	}

	/**
	 * Returns if the QoTO is acceptable or not
	 * 
	 * @return the QoTForOther
	 */
	public boolean isQoTForOther() {
		return QoTForOther;
	}

	/**
	 * Sets if the QoTO is acceptable or not
	 * 
	 * @param qoTForOther the qoTForOther to set
	 */
	public void setQoTForOther(boolean QoTForOther) {
		this.QoTForOther = QoTForOther;
	}
	
	/**
	 * This method returns the spectrum allocated by the circuit on a link
     * Can change according to the type of circuit
	 * 
	 * @param link - Link
	 * @return int[]
	 */
	public int[] getSpectrumAssignedByLink(Link link){
		int sa[] = getSpectrumAssigned();
		return sa;
	}
	
	/**
	 * This method that returns the modulation format used in a given route link
	 * Can change according to the type of circuit
	 * 
	 * @param link - Link
	 * @return Modulation
	 */
	public Modulation getModulationByLink(Link link){
		Modulation mod = getModulation();
		return mod;
	}

	/**
	 * Returns the power consumption by circuit
	 * 
	 * @return the powerConsumption
	 */
	public double getPowerConsumption() {
		return powerConsumption;
	}

	/**
	 * Sets the power consumption by circuit
	 * 
	 * @param powerConsumption the powerConsumption to set
	 */
	public void setPowerConsumption(double powerConsumption) {
		this.powerConsumption = powerConsumption;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId(){
		return this.id;
	}
	
	/**
	 * @return int
	 */
	@Override
	public int compareTo(Object o) {
		return this.id.compareTo(((Circuit)o).getId());
	}
	
	/**
	 * @return the hash code
	 */
	@Override
	public int hashCode(){
		return this.id * 31;
	}
	
	/**
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if(o != null){
			return this.id == ((Circuit)o).getId();
		}
		return false;
	}

	/**
	 * Returns the total bit rate of the circuit
	 * 
	 * @return double
	 */
	public double getBandwidth(){
		return getModulation().potentialBandwidth(spectrumAssigned[1] - spectrumAssigned[0] + 1);
	}
	
	/**
	 * Returns the launch power
	 * 
	 * @return double
	 */
	public double getLaunchPowerLinear() {
		return launchPowerLinear;
	}
	
	/** 
	 * Sets the launch power
	 * 
	 * @param launchPowerLinear double
	 */
	public void setLaunchPowerLinear(double launchPowerLinear) {
		this.launchPowerLinear = launchPowerLinear;
	}
	
	/**
	 * Returns the guard band
	 * 
	 * @return int
	 */
	public int getGuardBand() {
		return guardBand;
	}

	/**
	 * Sets the guard band
	 * 
	 * @param guardBand int
	 */
	public void setGuardBand(int guardBand) {
		this.guardBand = guardBand;
	}

}

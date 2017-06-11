package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import request.RequestForConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an established circuit in an optical network
 * 
 * @author Iallen
 */
public class Circuit {

    protected Pair pair;
    protected Route route;
    protected int spectrumAssigned[];
    protected Modulation modulation;
    protected List<RequestForConnection> requests; // Requests attended by this circuit
    
    protected double SNR; //dB
	protected boolean QoT;
	protected boolean QoTForOther;

    /**
     * Instantiates a circuit with the list of requests answered by it in empty
     */
    public Circuit() {
        requests = new ArrayList<>();
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
    public void setSpectrumAssigned(int sa[]) {
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
	 * @return the sNR
	 */
	public double getSNR() {
		return SNR;
	}

	/**
	 * @param sNR the sNR to set
	 */
	public void setSNR(double sNR) {
		SNR = sNR;
	}

	/**
	 * @return the qoT
	 */
	public boolean isQoT() {
		return QoT;
	}

	/**
	 * @param qoT the qoT to set
	 */
	public void setQoT(boolean qoT) {
		QoT = qoT;
	}

	/**
	 * @return the qoTForOther
	 */
	public boolean isQoTForOther() {
		return QoTForOther;
	}

	/**
	 * @param qoTForOther the qoTForOther to set
	 */
	public void setQoTForOther(boolean qoTForOther) {
		QoTForOther = qoTForOther;
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
}

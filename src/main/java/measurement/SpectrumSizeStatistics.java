package measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import request.RequestForConnection;
import simulationControl.resultManagers.SpectrumSizeStatisticsResultManager;

/**
 * This class represents the metric that computes the number of requests that use a given number of slots.
 * 
 * @author Iallen
 */
public class SpectrumSizeStatistics extends Measurement{
	public final static String SEP = "-";
	
	/**
	 * Stores the number of requests generated based on the number of slots that these requests require
	 */
	private HashMap<Integer, Integer> numberReqPerSlotReq;	
	private int numberRequests;
	
	private HashMap<String, HashMap<Integer,Integer>> numberReqPerSlotReqPerLink;
	private HashMap<String, Integer> numberRequestsPerLink;
	
	/**
	 * Creates a new instance of SpectrumSizeStatistics
	 * 
	 * @param loadPoint int
	 * @param replication int
	 */
	public SpectrumSizeStatistics(int loadPoint, int replication){
		super(loadPoint, replication);
		
		numberRequests = 0;
		numberReqPerSlotReq = new HashMap<>();
		numberRequestsPerLink = new HashMap<>();
		numberReqPerSlotReqPerLink = new HashMap<>();
		
		fileName = "_SpectrumSizeStatistics.csv";
		resultManager = new SpectrumSizeStatisticsResultManager();
	}
	
	/**
	 * Adds a new observation of slots utilization
	 * 
	 * @param cp ControlPlane
     * @param success boolean
     * @param request RequestForConnection
	 */
	public void addNewObservation(ControlPlane cp, boolean success, RequestForConnection request){
		if(request.getCircuit().getModulation() == null) // This metric may not be reliable if there are locks due to lack of transmitter
			return;
		this.newObservationRequestSizeBandwidthGeneral(request.getCircuit());	
		this.newObservationRequestSizeBandwidthPerLink(request.getCircuit());
	}
	
	/**
	 * Observation of the Requisition metric according to the size of the band requested in general
	 * 
	 * @param request
	 */
	private void newObservationRequestSizeBandwidthGeneral(Circuit request){
		numberRequests++;			
		int qSlots = request.getModulation().requiredSlots(request.getRequiredBandwidth());			
		Integer aux = this.numberReqPerSlotReq.get(qSlots);			
		if(aux==null){
			aux = 0;
		}			
		aux++;			
		this.numberReqPerSlotReq.put(qSlots, aux);		
	}
	
	/** 
	 * Observation of the Requisition metric according to the size of the band requested per link
	 * 
	 * @param request
	 */
	private void newObservationRequestSizeBandwidthPerLink(Circuit request){
		for (Link link : request.getRoute().getLinkList()) {
			newObsReqSizeBandPerLink(link, request);
		}	
	}
	
	/**
	 * Observation of the Requisition metric according to the size of the band requested per link
	 * 
	 * @param link
	 * @param request
	 */
	private void newObsReqSizeBandPerLink(Link link, Circuit request){
		String l = link.getSource().getName() + SEP + link.getDestination().getName();
		
		// Increase the number of requests generated
		Integer aux = this.numberRequestsPerLink.get(l);
		if(aux == null) aux = 0;
		aux++;
		this.numberRequestsPerLink.put(l, aux);
		
		// Increase the number of requisitions generated with this requested range size
		int qSlots = request.getModulation().requiredSlots(request.getRequiredBandwidth());
		HashMap<Integer, Integer> hashAux = numberReqPerSlotReqPerLink.get(l);
		if(hashAux==null){
			hashAux = new HashMap<Integer, Integer>();
			numberReqPerSlotReqPerLink.put(l, hashAux);
		}
		aux = hashAux.get(qSlots);
		if(aux==null) aux = 0;
		aux++;
		hashAux.put(qSlots, aux);		
	}
	
	/**
	 * Returns a list containing the values of range sizes that have had at least one request
	 * 
	 * @return List<Integer>
	 */
	public List<Integer> getNumberOfSlotsList(){
		ArrayList<Integer> res = new ArrayList<Integer>(this.numberReqPerSlotReq.keySet());
		
		return res;
	}
	
	/**
	 * Returns the percentage of requests among those that were generated that required a free range of 
	 * size passed by parameter
	 * 
	 * @param sizeOfTheRequestedBand
	 * @return double
	 */
	public double getPercentageReq(int tamanhoFaixaReq){
		try{
			return ((double)this.numberReqPerSlotReq.get(tamanhoFaixaReq))/((double)this.numberRequests);
		}catch(NullPointerException npex){
			return 0.0;
		}
	}
	
	/**
	 * Returns the HashMap key set
     * The key set corresponds to the links that were analyzed by the metric
     * 
	 * @return Set<String>
	 */
	public Set<String> getLinkSet(){
		return this.numberReqPerSlotReqPerLink.keySet();		
	}
	
	/**
	 * Returns the list of the number of slots for a given link
	 * 
	 * @param link String
	 * @return List<Integer>
	 */
	public List<Integer> getNumberOfSlotsPerLink(String link){
		ArrayList<Integer> res = new ArrayList<Integer>(this.numberReqPerSlotReqPerLink.get(link).keySet());
		
		return res;
	}
	
	/**
	 * Returns the percentage of slots requested for requests
	 * 
	 * @param link String
	 * @param sizeOfTheRequestedBand int
	 * @return double
	 */
	public double getPercentageReq(String link, int tamanhoFaixaReq){
		try{
			double d = this.numberReqPerSlotReqPerLink.get(link).get(tamanhoFaixaReq);
			double n = this.numberRequestsPerLink.get(link);
			return d/n;
		}catch(NullPointerException npe){
			return 0.0;
		}
	}
	
}

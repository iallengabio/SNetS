package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.BandwidthBlockingProbability;
import network.Pair;
import simulationControl.Util;

/**
 * This class is responsible for formatting the file with results of bandwidth blocking probability
 * 
 * @author Iallen
 */
public class BandwidthBlockingProbResultManager {
	
	private HashMap<Integer, HashMap<Integer, BandwidthBlockingProbability>> pbs; // Contains the bandwidth blocking probability metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private List<Pair> pairs;
	private final static String sep = ",";
	
	/**
	 * Creates a new instance of BandwidthBlockingProbResultManager
	 * 
	 * @param lpdb List<List<BandwidthBlockingProbability>>
	 */
	public BandwidthBlockingProbResultManager(List<List<BandwidthBlockingProbability>> lpdb){
		pbs = new HashMap<>();
		
		for (List<BandwidthBlockingProbability> loadPoint : lpdb) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, BandwidthBlockingProbability>  reps = new HashMap<>();
			pbs.put(load, reps);
			
			for (BandwidthBlockingProbability pb : loadPoint) {
				reps.put(pb.getReplication(), pb);
			}			
		}
		loadPoints = new ArrayList<>(pbs.keySet());
		replications = new ArrayList<>(pbs.values().iterator().next().keySet());
		this.pairs = new ArrayList<>(Util.pairs);
	}
	
	/**
	 * Returns a string corresponding to the result file for bandwidth blocking probabilities
	 * 
	 * @return String
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Bandwidth" + sep + "src" + sep + "dest" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeneral());
		res.append("\n\n");
		res.append(resultPair());
		res.append("\n\n");
		res.append(resultBandwidth());
		res.append("\n\n");
		res.append(resultPairBandwidth());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the bandwidth blocking probability general
	 * 
	 * @return String
	 */
	private String resultGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("bandwidth blocking probability" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockGeneral());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Returns the bandwidth blocking probabilities per pair
	 * 
	 * @return String
	 */
	private String resultPair(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per pair" + sep + loadPoint + sep + "all";
			
			for (Pair pair : this.pairs) {
				String aux2 = aux + sep + pair.getSource().getName() + sep + pair.getDestination().getName() + sep + " ";
				for (Integer replic : replications) {
					aux2 = aux2 + sep + pbs.get(loadPoint).get(replic).getProbBlockPair(pair);
				}
				res.append(aux2 + "\n");	
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the bandwidth blocking probabilities per bandwidth
	 * 
	 * @return String
	 */
	private String resultBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per bandwidth" + sep + loadPoint;
			
			for (Double bandwidth : Util.bandwidths) {
				String aux2 = aux + sep + (bandwidth/1000000000.0) + "Gbps" + sep + "all" + sep + "all" + sep + " ";
				for (Integer rep : replications) {
					aux2 = aux2 + sep + pbs.get(loadPoint).get(rep).getProbBlockBandwidth(bandwidth);
				}
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}

	/**
	 * Returns the bandwidth blocking probabilities per pair and bandwidth
	 * 
	 * @return
	 */
	private String resultPairBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per pair and bandwidth" + sep + loadPoint;
			
			for (Double bandwidth : Util.bandwidths) {
				String aux2 = aux + sep + (bandwidth/1000000000.0) + "Gbps";
				for (Pair pair :  Util.pairs) {
					String aux3 = aux2 + sep + pair.getSource().getName() + sep + pair.getDestination().getName() + sep + " ";
					for(Integer rep :  replications){
						aux3 = aux3 + sep + pbs.get(loadPoint).get(rep).getProbBlockPairBandwidth(pair, bandwidth);
					}
					res.append(aux3 + "\n");
				}				
			}
		}
		return res.toString();
	}
	
}

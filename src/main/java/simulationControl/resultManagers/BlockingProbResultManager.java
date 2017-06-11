package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.BlockingProbability;
import network.Pair;
import simulationControl.Util;

/**
 * This class is responsible for formatting the file with results of circuit blocking probability
 * 
 * @author Iallen
 */
public class BlockingProbResultManager {
	
	private HashMap<Integer, HashMap<Integer, BlockingProbability>> pbs; // Contains the blocking probability metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private List<Pair> pairs;
	private final static String sep = ",";
	
	/**
	 * Creates a new instance of BlockingProbResultManager
	 * 
	 * @param lpdb List<List<BlockingProbability>> 
	 */
	public BlockingProbResultManager(List<List<BlockingProbability>> lpdb){
		pbs = new HashMap<>();
		
		for (List<BlockingProbability> loadPoint : lpdb) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, BlockingProbability>  reps = new HashMap<>();
			pbs.put(load, reps);
			
			for (BlockingProbability pb : loadPoint) {
				reps.put(pb.getReplication(), pb);
			}			
		}
		loadPoints = new ArrayList<>(pbs.keySet());
		replications = new ArrayList<>(pbs.values().iterator().next().keySet());
		this.pairs = new ArrayList<>(Util.pairs);
	} 
	
	/**
	 * Returns a string corresponding to the result file for blocking probabilities
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
		res.append(resultGeneralLackTx());
		res.append("\n\n");
		res.append(resultGeneralLackRx());
		res.append("\n\n");
		res.append(resultGeneralFrag());
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
	 * Returns the blocking probability general
	 * 
	 * @return String
	 */
	private String resultGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("blocking probability" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockGeneral());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability per lack of transmitters
	 * 
	 * @return String
	 */
	private String resultGeneralLackTx(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("blocking probability per lack of transmitters" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockLackTxGen());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability per lack of receivers
	 * 
	 * @return String
	 */
	private String resultGeneralLackRx(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("blocking probability per lack of receivers" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockLackRxGen());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the blocking probability per fragmentation
	 * 
	 * @return String
	 */
	private String resultGeneralFrag(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("blocking probability per fragmentation" + sep + loadPoint + sep + "all" + sep + "all" + sep + "all" + sep + " ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockFragGeneral());
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Returns the blocking probability per pair
	 * 
	 * @return String
	 */
	private String resultPair(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "blocking probability per pair" + sep + loadPoint + sep + "all";
			
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
	 * Returns the blocking probability per bandwidth
	 * 
	 * @return String
	 */
	private String resultBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "blocking probability per bandwidth" + sep + loadPoint;
			
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
	 * Returns the blocking probability per pair and bandwidth
	 * 
	 * @return String
	 */
	private String resultPairBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "blocking probability per pair and bandwidth" + sep + loadPoint;
			
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

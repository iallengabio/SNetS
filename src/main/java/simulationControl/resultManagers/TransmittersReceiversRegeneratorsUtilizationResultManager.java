package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.Measurement;
import measurement.TransmittersReceiversRegeneratorsUtilization;

/**
 * This class is responsible for formatting the file with results of transmitters, 
 * receivers, and regenerators utilization
 * 
 * @author Iallen
 */
public class TransmittersReceiversRegeneratorsUtilizationResultManager implements ResultManagerInterface {
	
	private HashMap<Integer, HashMap<Integer, TransmittersReceiversRegeneratorsUtilization>> trus; // Contains the transmitters, receivers, and regenerators utilization metric for all load points and replications
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	/**
	 * This method organizes the data by load point and replication.
	 * 
	 * @param llms List<List<Measurement>>
	 */
	public void config(List<List<Measurement>> llms){
		trus = new HashMap<>();
		
		for (List<Measurement> loadPoint : llms) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, TransmittersReceiversRegeneratorsUtilization>  reps = new HashMap<>();
			trus.put(load, reps);
			
			for (Measurement tru : loadPoint) {
				reps.put(tru.getReplication(), (TransmittersReceiversRegeneratorsUtilization)tru);
			}			
		}
		loadPoints = new ArrayList<>(trus.keySet());
		replications = new ArrayList<>(trus.values().iterator().next().keySet());
	} 
	
	/**
	 * Returns a string corresponding to the result file for transmitters, receivers, and regenerators utilization
	 * 
	 * @return String
	 */
	public String result(List<List<Measurement>> llms){
		config(llms);
		
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep + "LoadPoint" + sep + "Node" + sep + " ");
		
		for (Integer rep : replications) { // Checks how many replications have been made and creates the header of each column
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultTxUtilizationGeneral());
		res.append("\n\n");
		res.append(resultRxUtilizationGeneral());
		res.append("\n\n");
		res.append(resultRegenUtilizationGeneral());
		res.append("\n\n");
		
		res.append(resultTxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultRxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultRegenUtilizationPerNode());
		res.append("\n\n");
		
		res.append(resultMaxTxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultMaxRxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultMaxRegenUtilizationPerNode());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * Returns the transmitter utilization general
	 * 
	 * @return String
	 */
	private String resultTxUtilizationGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Tx Utilization" + sep + loadPoint + sep + "all" + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + trus.get(loadPoint).get(replic).getAvgTxUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the receivers utilization general
	 * 
	 * @return String
	 */
	private String resultRxUtilizationGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Rx Utilization" + sep + loadPoint + sep + "all" + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + trus.get(loadPoint).get(replic).getAvgRxUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the regenerators utilization general
	 * 
	 * @return String
	 */
	private String resultRegenUtilizationGeneral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Regenerators Utilization" + sep + loadPoint + sep + "all" + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + trus.get(loadPoint).get(replic).getAvgRegenUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	/**
	 * Returns the transmitters utilization per node
	 * 
	 * @return String
	 */
	private String resultTxUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Tx Utilization Per Node" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getAvgTxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the receivers utilization per node
	 * 
	 * @return String
	 */
	private String resultRxUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Rx Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getAvgRxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the regenerators utilization per node
	 * 
	 * @return String
	 */
	private String resultRegenUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Regenerators Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getAvgRegenUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the maximum transmitters utilization per node
	 * 
	 * @return String
	 */
	private String resultMaxTxUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Max Tx Utilization Per Node" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getMaxTxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	/**
	 * Returns the maximum receivers utilization per node
	 * 
	 * @return String
	 */
    private String resultMaxRxUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Max Rx Utilization Per Node" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getMaxRxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
    
    /**
	 * Returns the maximum regenerators utilization per node
	 * 
	 * @return String
	 */
    private String resultMaxRegenUtilizationPerNode(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Max Regenerators Utilization Per Node" + sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(0).get(0).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getMaxRegenUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
}

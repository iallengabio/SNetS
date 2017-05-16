package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import measurement.TransmitersReceiversUtilization;
import measurement.UtilizacaoSpectro;
import network.Link;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de utilização de recursos
 * @author Iallen
 *
 */
public class TransmitersReceiversUtilizationResultManager {
	
	private HashMap<Integer, HashMap<Integer, TransmitersReceiversUtilization>> trus; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	public TransmitersReceiversUtilizationResultManager(List<List<TransmitersReceiversUtilization>> ltru){
		trus = new HashMap<>();
		
		for (List<TransmitersReceiversUtilization> loadPoint : ltru) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, TransmitersReceiversUtilization>  reps = new HashMap<>();
			trus.put(load, reps);
			
			for (TransmitersReceiversUtilization su : loadPoint) {
				reps.put(su.getReplication(), su);
			}			
		}
		loadPoints = new ArrayList<>(trus.keySet());
		replications = new ArrayList<>(trus.values().iterator().next().keySet());
		
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado para a fragmentacao externa
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"load point"+sep+"node"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultTxUtilizationGeral());
		res.append("\n\n");
		res.append(resultRxUtilizationGeral());
		res.append("\n\n");
		res.append(resultTxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultRxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultMaxTxUtilizationPerNode());
		res.append("\n\n");
		res.append(resultMaxRxUtilizationPerNode());
		
		return res.toString();
	}
	
	
	
	
	
	private String resultTxUtilizationGeral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Tx Utilization "+sep + loadPoint + sep + "all" + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + trus.get(loadPoint).get(replic).getAvgTxUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	private String resultRxUtilizationGeral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Rx Utilization "+sep + loadPoint + sep + "all" + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + trus.get(loadPoint).get(replic).getAvgRxUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	private String resultTxUtilizationPerNode(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Tx Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(1).get(1).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getAvgTxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	private String resultRxUtilizationPerNode(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Rx Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(1).get(1).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getAvgRxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	private String resultMaxTxUtilizationPerNode(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Max Tx Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(1).get(1).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getMaxTxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
private String resultMaxRxUtilizationPerNode(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Max Rx Utilization Per Node"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String node : trus.get(1).get(1).getNodeNamesSet()) {
				String aux2 = aux + node + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + trus.get(loadPoint).get(replic).getMaxRxUtilizationPerNode(node);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}

	
}

package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.ProbabilidadeDeBloqueioDeBanda;
import network.Pair;
import simulationControl.Util;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de probabilidade de bloqueio de banda
 * @author Iallen
 *
 */
public class BandwidthBlockingProbResultManager {
	
	private HashMap<Integer, HashMap<Integer, ProbabilidadeDeBloqueioDeBanda>> pbs; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private List<Pair> pairs;
	private final static String sep = ",";
	
	public BandwidthBlockingProbResultManager(List<List<ProbabilidadeDeBloqueioDeBanda>> lpdb){
		pbs = new HashMap<>();
		
		for (List<ProbabilidadeDeBloqueioDeBanda> loadPoint : lpdb) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, ProbabilidadeDeBloqueioDeBanda>  reps = new HashMap<>();
			pbs.put(load, reps);
			
			for (ProbabilidadeDeBloqueioDeBanda pb : loadPoint) {
				reps.put(pb.getReplication(), pb);
			}			
		}
		loadPoints = new ArrayList<>(pbs.keySet());
		replications = new ArrayList<>(pbs.values().iterator().next().keySet());
		this.pairs = new ArrayList<>(Util.pairs);
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado para probabilidades de bloqueio de banda
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"LoadPoint"+sep+"Bandwidth"+sep+"src"+sep+"dest"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeral());
		res.append("\n\n");
		res.append(resultPair());
		res.append("\n\n");
		res.append(resultBandwidth());
		res.append("\n\n");
		res.append(resultPairBandwidth());
		res.append("\n\n");
		
		return res.toString();
	}
	
	private String resultGeral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("bandwidth blocking probability"+sep + loadPoint + sep + "all"+sep+"all"+sep+"all"+sep+" ");
			for (Integer replic : replications) {
				res.append(sep + pbs.get(loadPoint).get(replic).getProbBlockGeral());
			}
			res.append("\n");
		}
		return res.toString();
	}

	private String resultPair(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per pair"+sep + loadPoint + sep + "all";
			
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
	
	private String resultBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per bandwidth"+sep + loadPoint;
			
			for (Double bandwidth : Util.bandwidths) {
				String aux2 = aux + sep + (bandwidth/1073741824.0) + "Gbps" + sep + "all" + sep + "all" + sep + " ";
				for (Integer rep : replications) {
					aux2 = aux2 + sep + pbs.get(loadPoint).get(rep).getProbBlockBandwidth(bandwidth);
				}
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}

	private String resultPairBandwidth(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "bandwidth blocking probabilities per pair and bandwidth"+sep + loadPoint;
			
			for (Double bandwidth : Util.bandwidths) {
				String aux2 = aux + sep + (bandwidth/1073741824.0) + "Gbps";
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

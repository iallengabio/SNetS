package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.SpectrumSizeStatistics;
import measurement.UtilizacaoSpectro;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de utilização de recursos
 * @author Iallen
 *
 */
public class SpectrumSizeStatisticsResultManager {
	
	private HashMap<Integer, HashMap<Integer, SpectrumSizeStatistics>> ssss; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	public SpectrumSizeStatisticsResultManager(List<List<SpectrumSizeStatistics>> lsu){
		ssss = new HashMap<>();
		
		for (List<SpectrumSizeStatistics> loadPoint : lsu) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, SpectrumSizeStatistics>  reps = new HashMap<>();
			ssss.put(load, reps);
			
			for (SpectrumSizeStatistics su : loadPoint) {
				reps.put(su.getReplication(), su);
			}			
		}
		loadPoints = new ArrayList<>(ssss.keySet());
		replications = new ArrayList<>(ssss.values().iterator().next().keySet());
		
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"load point"+sep+"link"+sep+"number of slots"+sep+"slot"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
				
		res.append(resultLarguraEspectroReqGeral());
		res.append("\n\n");
		res.append(resultLarguraEspectroPerLink());
		res.append("\n\n");
		
		return res.toString();
	}
	
	/**
	 * formata o resultado da porcao de requisicoes que exige cada largura de espectro
	 * @return
	 */
	private String resultLarguraEspectroReqGeral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Requisitons per number of slots"+sep + loadPoint + sep + "all"+sep; // "all"+sep+" ";
			for (Integer numSlots : ssss.get(1).get(1).getQuantidadesDeSlots()) {
				String aux2 = aux + numSlots + sep + "-" + sep + " ";
				for (Integer replic : replications) {
					aux2 = aux2 + sep + ssss.get(loadPoint).get(replic).getPercentualReq(numSlots);
				}			
				res.append(aux2 + "\n");
			}		
		}
		return res.toString();
	}
	
	/**
	 * formata o resultado da porcao de requisicoes que exige cada largura de espectro por link
	 * @return
	 */
	private String resultLarguraEspectroPerLink(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Requisitons per number of slots per link"+sep + loadPoint + sep; // "all"+sep+" ";
			for (String link : ssss.get(1).get(1).getLinkSet()) {
				String aux2 = aux + "<"+link+">" + sep;
				for (Integer numSlots : ssss.get(1).get(1).getQuantidadesDeSlotsPorLink(link)) {
					String aux3 = aux2 + numSlots + sep + "-" + sep + " ";
					for (Integer replic : replications) {
						aux3 = aux3 + sep + ssss.get(loadPoint).get(replic).getPercentualReq(link, numSlots);
					}	
					res.append(aux3 + "\n");
				}					
			}
		}
		return res.toString();	
	}
	
	

	
}

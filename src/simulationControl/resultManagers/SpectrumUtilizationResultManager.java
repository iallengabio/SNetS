package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import measurement.UtilizacaoSpectro;
import network.Link;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de utilização de recursos
 * @author Iallen
 *
 */
public class SpectrumUtilizationResultManager {
	
	private HashMap<Integer, HashMap<Integer, UtilizacaoSpectro>> sus; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	public SpectrumUtilizationResultManager(List<List<UtilizacaoSpectro>> lsu){
		sus = new HashMap<>();
		
		for (List<UtilizacaoSpectro> loadPoint : lsu) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, UtilizacaoSpectro>  reps = new HashMap<>();
			sus.put(load, reps);
			
			for (UtilizacaoSpectro su : loadPoint) {
				reps.put(su.getReplication(), su);
			}			
		}
		loadPoints = new ArrayList<>(sus.keySet());
		replications = new ArrayList<>(sus.values().iterator().next().keySet());
		
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado para a fragmentacao externa
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"load point"+sep+"link"+sep+"number of slots"+sep+"slot"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultUtilizationGeral());
		res.append("\n\n");
		res.append(resultUtilizationPerLink());
		res.append("\n\n");
		
		res.append(resultUtilizationPerSlot());
		
		
		return res.toString();
	}
	
	
	
	
	
	private String resultUtilizationGeral(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Utilization "+sep + loadPoint + sep + "all"+sep + " - " + sep + " - " + sep + " "; // "all"+sep+" ";
			for (Integer replic : replications) {
					aux = aux + sep + sus.get(loadPoint).get(replic).getUtilizationGen();
			}
			res.append(aux + "\n");
		}
		return res.toString();
	}
	
	private String resultUtilizationPerLink(){
		
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			
			String aux = "Utilization Per Link"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String link : sus.get(1).get(1).getLinkSet()) {
				String aux2 = aux + "<"+link+">" + sep + " - " + sep + " - " + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + sus.get(loadPoint).get(replic).getUtilizationPerLink(link);
				}	
				res.append(aux2 + "\n");
			}
		}
		return res.toString();
	}
	
	private String resultUtilizationPerSlot(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "Utilization Per Slot"+sep + loadPoint + sep + "all" + sep + " - " + sep;
			int i;
			for(i=1;i<=400;i++){
				String aux2 = aux + i + sep + " ";
				for (Integer rep : replications) {
					aux2 = aux2 + sep + sus.get(loadPoint).get(rep).getUtilizationPerSlot(i);
				}
				res.append(aux2 + "\n");
			}
			
		}
		return res.toString();
	}
	
}

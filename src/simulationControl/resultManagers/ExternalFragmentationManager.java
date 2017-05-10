package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.FragmentacaoExterna;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de probabilidade de bloqueio de circuitos
 * @author Iallen
 *
 */
public class ExternalFragmentationManager {
	
	private HashMap<Integer, HashMap<Integer, FragmentacaoExterna>> fes; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	public ExternalFragmentationManager(List<List<FragmentacaoExterna>> lfe){
		fes = new HashMap<>();
		
		for (List<FragmentacaoExterna> loadPoint : lfe) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, FragmentacaoExterna>  reps = new HashMap<>();
			fes.put(load, reps);
			
			for (FragmentacaoExterna fe : loadPoint) {
				reps.put(fe.getReplication(), fe);
			}			
		}
		loadPoints = new ArrayList<>(fes.keySet());
		replications = new ArrayList<>(fes.values().iterator().next().keySet());
		
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado para a fragmentacao externa
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"LoadPoint"+sep+"link"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeralVertical());
		res.append("\n\n");
		res.append(resultGeralHorizontal());
		res.append("\n\n");
		res.append(resultPerLink());
		res.append("\n\n");
		
		return res.toString();
	}

	private String resultGeralVertical() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("External Fragmentation (Vertical)"+sep + loadPoint + sep + "all"+sep+" ");
			for (Integer replic : replications) {
				res.append(sep + fes.get(loadPoint).get(replic).getFEVertical());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String resultGeralHorizontal() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			res.append("External Fragmentation (Horizontal)"+sep + loadPoint + sep + "all"+sep+" ");
			for (Integer replic : replications) {
				res.append(sep + fes.get(loadPoint).get(replic).getFEHorizontal());
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	private String resultPerLink(){
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			String aux = "External Fragmentation Per Link"+sep + loadPoint + sep; // "all"+sep+" ";
			
			for (String link : fes.get(1).get(1).getLinkSet()) {
				String aux2 = aux + "<" + link + ">" + sep + " ";
				
				for (Integer replic : replications) {
					aux2 = aux2 + sep + fes.get(loadPoint).get(replic).getFeLink(link);
				}
				res.append(aux2 + "\n");
			}
			
		}
		return res.toString();
	}
	
}

package simulationControl.resultManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import measurement.FragmentacaoExterna;
import measurement.FragmentacaoRelativa;

/**
 * esta classe eh responsavel por formatar o arquivo com resultados de fragmentacao relativa
 * @author Iallen
 *
 */
public class RelativeFragmentationManager {
	
	private HashMap<Integer, HashMap<Integer, FragmentacaoRelativa>> frs; //contem a metrica probabilidade de bloqueio para todos os pontos de carga e replicacoes
	private List<Integer> loadPoints;
	private List<Integer> replications;
	private final static String sep = ",";
	
	public RelativeFragmentationManager(List<List<FragmentacaoRelativa>> lfr){
		frs = new HashMap<>();
		
		for (List<FragmentacaoRelativa> loadPoint : lfr) {
			int load = loadPoint.get(0).getLoadPoint();
			HashMap<Integer, FragmentacaoRelativa>  reps = new HashMap<>();
			frs.put(load, reps);
			
			for (FragmentacaoRelativa fr : loadPoint) {
				reps.put(fr.getReplication(), fr);
			}			
		}
		loadPoints = new ArrayList<>(frs.keySet());
		replications = new ArrayList<>(frs.values().iterator().next().keySet());
		
	} 
	
	/**
	 * retorna uma string correspondente ao arquivo de resultado para a fragmentacao externa
	 * @return
	 */
	public String result(){
		StringBuilder res = new StringBuilder();
		res.append("Metrics" + sep +"LoadPoint"+sep+"link"+sep+"spectrum size (c)"+sep+" ");
		
		for (Integer rep : replications) { //verifica quantas replicacoes foram feitas e cria o cabecalho de cada coluna
			res.append(sep + "rep" + rep);
		}
		res.append("\n");
		
		res.append(resultGeral());
		res.append("\n\n");
		
		return res.toString();
	}

	private String resultGeral() {
		StringBuilder res = new StringBuilder();
		for (Integer loadPoint : loadPoints) {
			for (Integer c : this.frs.get(1).get(1).getCList()) {
				String aux = "Relative Fragmentation per spectrum size (c)" + sep + loadPoint + sep + "all" + sep + c + sep + " ";
				for (Integer replication : replications) {
					aux = aux + sep + frs.get(loadPoint).get(replication).getFragmentacaoRelativaMedia(c);
				}
				res.append(aux + "\n");
			}
		}
		return res.toString();
	}
	
}

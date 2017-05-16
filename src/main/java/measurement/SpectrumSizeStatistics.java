package measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import network.Circuit;
import network.Link;

public class SpectrumSizeStatistics extends Measurement{
	public final static String SEP = "-";
	
	
	
	/**
	 * armazena a quantidade de requisi��es geradas em fun��o da quantidade de slots que estas requisi��es exigem
	 */
	private HashMap<Integer, Integer> quantReqPerSlotReq;	
	private int quantRequisicoes;
	
	private HashMap<String, HashMap<Integer,Integer>> quantReqPerSlotReqPerLink;
	private HashMap<String, Integer> quantRequisicoesPerLink;
	
	
	public SpectrumSizeStatistics(int loadPoint, int replication){
		super(loadPoint, replication);
		
		quantRequisicoes = 0;
		quantReqPerSlotReq = new HashMap<>();
		quantRequisicoesPerLink = new HashMap<>();
		quantReqPerSlotReqPerLink = new HashMap<>();
		
	}
	
	/**
	 * adiciona uma nova observa��o de utiliza��o
	 * @param request
	 */
	public void addNewObservation(Circuit request){
		if(request.getModulation()==null) return;//esta m�trica pode n�o ser confi�vel caso haja bloqueios por falta de transmissor
		this.newObsReqTamFaixaGeral(request);	
		this.newObsReqTamFaixaPerLink(request);
	}
	
	/** 
	 * observa��o da m�trica de Requisi��es em fun��o do tamanho da faixa requisitado de forma geral
	 * @param request
	 */
	private void newObsReqTamFaixaGeral(Circuit request){
		
		quantRequisicoes++;			
		int qSlots = request.getModulation().requiredSlots(request.getRequiredBandwidth());			
		Integer aux = this.quantReqPerSlotReq.get(qSlots);			
		if(aux==null){
			aux = 0;
		}			
		aux++;			
		this.quantReqPerSlotReq.put(qSlots, aux);		
	}
	
	/** 
	 * observa��o da m�trica de Requisi��es em fun��o do tamanho da faixa requisitado por link
	 * @param request
	 */
	private void newObsReqTamFaixaPerLink(Circuit request){
		
		for (Link link : request.getRoute().getLinkList()) {
			newObsReqTamFaixaPerLink(link, request);
		}	
	}
	
	private void newObsReqTamFaixaPerLink(Link link, Circuit request){
		String l = link.getSource().getName() + SEP + link.getDestination().getName();
		//incrementar a quantidade de requisi��es geradas
		Integer aux = this.quantRequisicoesPerLink.get(l);
		if(aux == null) aux = 0;
		aux++;
		this.quantRequisicoesPerLink.put(l, aux);
		
		//incrementar a quantidade de requisi��es geradas com este tamanho de faixa requisitado
		int qSlots = request.getModulation().requiredSlots(request.getRequiredBandwidth());
		HashMap<Integer, Integer> hashAux = quantReqPerSlotReqPerLink.get(l);
		if(hashAux==null){
			hashAux = new HashMap<Integer, Integer>();
			quantReqPerSlotReqPerLink.put(l, hashAux);
		}
		aux = hashAux.get(qSlots);
		if(aux==null) aux = 0;
		aux++;
		hashAux.put(qSlots, aux);		
	}
	
	/**
	 * retorna uma lista contendo os valores de tamanhos de faixa que tiveram pelo menos uma requisi��o
	 * @return
	 */
	public List<Integer> getQuantidadesDeSlots(){
		ArrayList<Integer> res = new ArrayList<Integer>(this.quantReqPerSlotReq.keySet());
		
		return res;
	}
	
	/**
	 * retorna o percentual de requisi��es entre as que foram geradas que exigiram uma faixa livre de tamanho passado por par�metro
	 * @param tamanhoFaixaReq
	 * @return
	 */
	public double getPercentualReq(int tamanhoFaixaReq){
		try{
			return ((double)this.quantReqPerSlotReq.get(tamanhoFaixaReq))/((double)this.quantRequisicoes);
		}catch(NullPointerException npex){
			return 0.0;
		}
	}
	
	public Set<String> getLinkSet(){
		return this.quantReqPerSlotReqPerLink.keySet();		
	}
	
	public List<Integer> getQuantidadesDeSlotsPorLink(String link){
		ArrayList<Integer> res = new ArrayList<Integer>(this.quantReqPerSlotReqPerLink.get(link).keySet());
		
		return res;
	}
	
	public double getPercentualReq(String link, int tamanhoFaixaReq){
		try{
			double d = this.quantReqPerSlotReqPerLink.get(link).get(tamanhoFaixaReq);
			double n = this.quantRequisicoesPerLink.get(link);
			return d/n;
		}catch(NullPointerException npe){
			return 0.0;
		}
		
	}
	
	
	
	
}

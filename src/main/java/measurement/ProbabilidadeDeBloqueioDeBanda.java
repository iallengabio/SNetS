package measurement;

import java.util.HashMap;

import network.Pair;
import request.RequestForConnection;

/**
 * Esta classe representa a m�trica de probabilidade de bloqueio (geral, por par, por largura de banda, por par/larguraDeBanda)
 * A m�trica representada por esta classe est� associada a um ponto de carga e uma replica��o
 * @author Iallen
 *
 */
public class ProbabilidadeDeBloqueioDeBanda extends Measurement{
	
	public final static String SEP = "-";
	
	
	//probabilidade de bloqueio geral
	private double bandaRequisitadaGeral;
	private double bandaBloqueadaGeral;
		
	//probabilidade de bloqueio por par
	private HashMap<String, Double> bandaRequisitadaPair;
	private HashMap<String, Double> bandaBloqueadaPair;
	
	//probabilidade de bloqueio por largura de banda
	private HashMap<Double, Double> bandaRequisitadaBW;
	private HashMap<Double, Double> bandaBloqueadaBW;
	
	//probabilidade de bloqueio por par/larguraDeBanda
	private HashMap<String, HashMap<Double,Double>> bandaRequisitadaPairBW;
	private HashMap<String, HashMap<Double,Double>> bandaBloqueadaPairBW;
	
	public ProbabilidadeDeBloqueioDeBanda(int loadPoint, int rep){
		super(loadPoint, rep);
		//probabilidade de bloqueio geral
		this.bandaRequisitadaGeral=0;
		this.bandaBloqueadaGeral=0;

		this.bandaRequisitadaBW = new HashMap<>();
		this.bandaBloqueadaBW = new HashMap<>();
		this.bandaRequisitadaPair = new HashMap<>();
		this.bandaBloqueadaPair = new HashMap<>();
		this.bandaRequisitadaPairBW = new HashMap<>();
		this.bandaBloqueadaPairBW = new HashMap<>();
	}
	
	
	/**
	 * adiciona uma nova observa��o de bloqueio ou n�o de uma requisi��o
	 * @param sucess
	 * @param request
	 */
	public void addNewObservation(boolean sucess, RequestForConnection request){
		//calcular a quantidade de banda requisitada pelo circuito
		Double time = request.getTimeOfFinalizeHours() - request.getTimeOfRequestHours();
		Double banda = time * request.getRequiredBandwidth();
		
			//incrementar requisi��es geradas geral
			this.bandaRequisitadaGeral+=banda;			
			//incrementar requisi��es geradas por par
			Double i = this.bandaRequisitadaPair.get(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName());
			if(i==null) i=0.0;
			this.bandaRequisitadaPair.put(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName(), i+banda);			
			//incrementar requisi��es geradas por largura de banda
			i = this.bandaRequisitadaBW.get(request.getRequiredBandwidth());
			if(i==null) i=0.0;
			this.bandaRequisitadaBW.put(request.getRequiredBandwidth(), i+banda);			
			//incrementar requisi��es geradas por par/larguraDeBanda
			HashMap<Double,Double> gplb = this.bandaRequisitadaPairBW.get(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName());
			if(gplb==null){
				gplb = new HashMap<>();
				this.bandaRequisitadaPairBW.put(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName(), gplb);
			}
			i = gplb.get(request.getRequiredBandwidth());
			if(i==null) i=0.0;
			gplb.put(request.getRequiredBandwidth(), i+banda);
			
			//caso haja bloqueio
			if(!sucess){
				//incrementar requisi��es bloqueadas geral
				this.bandaBloqueadaGeral+=banda;			
				//incrementar requisi��es bloqueadas por par
				i = this.bandaBloqueadaPair.get(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName());
				if(i==null) i=0.0;
				this.bandaBloqueadaPair.put(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName(), i+banda);			
				//incrementar requisi��es bloqueadas por largura de banda
				i = this.bandaBloqueadaBW.get(request.getRequiredBandwidth());
				if(i==null) i=0.0;
				this.bandaBloqueadaBW.put(request.getRequiredBandwidth(), i+banda);			
				//incrementar requisi��es bloqueadas por par/larguraDeBanda
				HashMap<Double,Double> bplb = this.bandaBloqueadaPairBW.get(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName());
				if(bplb==null){
					bplb = new HashMap<>();
					this.bandaBloqueadaPairBW.put(request.getPair().getSource().getName() + SEP +request.getPair().getDestination().getName(), bplb);
				}
				i = bplb.get(request.getRequiredBandwidth());
				if(i==null) i=0.0;
				bplb.put(request.getRequiredBandwidth(), i+banda);
			}			
	}
	
	/**
	 * retorna a probabilidade de bloqueio de banda geral na rede
	 * @return
	 */
	public double getProbBlockGeral(){
		return ((double) this.bandaBloqueadaGeral/ (double) this.bandaRequisitadaGeral);
	}
	
	/**
	 * retorna a probabilidade de bloqueio de banda de um determinado par
	 * @return
	 */
	public double getProbBlockPair(Pair p){
		double res;
		
		String or = p.getSource().getName();
		String dest = p.getDestination().getName();
		Double gen = this.bandaRequisitadaPair.get(or + SEP + dest);
		if(gen==null) return 0; //nenhuma requisi��o gerada para este par
		
		Double block = this.bandaBloqueadaPair.get(or + SEP + dest);
		if(block==null) block = 0.0;
		
		res = ((double) block / (double) gen);		
		
		return res;
	}

	/**
	 * retorna a probabilidade de bloqueio de banda de uma determinada largura de banda
	 * @param bw
	 * @return
	 */
	public double getProbBlockBandwidth(double bw){
		double res;
		Double gen = this.bandaRequisitadaBW.get(bw);
		if(gen==null) return 0; //nenhuma requisi��o gerada para este par
		
		Double block = this.bandaBloqueadaBW.get(bw);
		if(block==null) block = 0.0;
		
		res = ((double) block / (double) gen);		
		
		return res;
		
	}
	
	/**
	 * retorna a probabilidade de bloqueio de banda de uma determinada largura de banda em um determinado par
	 * @param bw
	 * @return
	 */
	public double getProbBlockPairBandwidth(Pair p, double bw){
		double res;
		String or = p.getSource().getName();
		String dest = p.getDestination().getName();
		Double gen = this.bandaRequisitadaPairBW.get(or + SEP + dest).get(bw);
		if(gen==null) return 0;//nenhuma requisi��o gerada para este par e largura de banda
		Double block = 0.0;
		
		HashMap<Double, Double> hashAux = this.bandaBloqueadaPairBW.get(or + SEP + dest);
		if(hashAux==null || hashAux.get(bw)==null){
			block = 0.0;
		}else{
			block = hashAux.get(bw);
		}
		
		
		res = ((double) block / (double) gen);		
		
		return res;
	}


	
	
}

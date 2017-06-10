package network;

import java.io.Serializable;

import request.RequestForConnection;

public class Regenerators implements Serializable {

	private double regenUtilization;
    private double numRegenerators;
    
    public Regenerators(int numberOfRegenerators){
    	this.regenUtilization = 0.0;
    	this.numRegenerators = numberOfRegenerators;
    }
    
    public boolean allocatesRegenerators(RequestForConnection request){
    	int numberOfRegenerators = getAmountOfRequiredRegenerators(request);
    	
    	if(quantRegeneratorsFree() >= numberOfRegenerators){
    		regenUtilization += numberOfRegenerators;
    		
    		return true;
    	}
		return false;
	}
	
	public boolean releasesRegenerators(RequestForConnection request){
		int numberOfRegenerators = getAmountOfRequiredRegenerators(request);
		
		if((regenUtilization - numberOfRegenerators) >= 0){
			regenUtilization -= numberOfRegenerators;
			
			return true;
		}
		return false;
	}

	public boolean regeneratorsFree(){
		return (regenUtilization < numRegenerators);
	}
	
	public double quantRegeneratorsFree(){
		return numRegenerators - regenUtilization;
	}

	/**
	 * @return the regenUtilization
	 */
	public double getRegenUtilization() {
		return regenUtilization;
	}

	/**
	 * @return the numRegenerators
	 */
	public double getNumRegenerators() {
		return numRegenerators;
	}

	/**
	 * @param numRegenerators the numRegenerators to set
	 */
	public void setNumRegenerators(double numRegenerators) {
		this.numRegenerators = numRegenerators;
	}
    
	/**
	 * Retorna a quantidade de regeneradores requeridos pela requisicao
	 * @param request - Request
	 * @return int - quantidade de regeneradores
	 */
	public int getAmountOfRequiredRegenerators(RequestForConnection request){
		double BR = 100.0; //Gbps
		double Bn = request.getRequiredBandwidth() / 1073741824.0;
		int quantRegeneradoresRequeridos = ComputeQoT.roundUp(Bn / BR);
		return quantRegeneradoresRequeridos;
	}
}

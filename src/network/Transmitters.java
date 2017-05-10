package network;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Transmitters implements Serializable {

	private int txUtilization;
	private int numberOfTx=1000000; //sem limitação de transmitters

	public Transmitters() {
		this.txUtilization = 0;
	}

	public Transmitters(int numberOfTx){
		this.numberOfTx = numberOfTx;
		this.txUtilization = 0;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Incrementa o num de transmissores que estao sendo utilizados e atualiza o
	 * somatorio e o pico de utilizacao de receptor
	 */
	public boolean alocTx() {
		if(this.txUtilization<this.numberOfTx){
			this.txUtilization++;
			return true;
		}else{
			return false;
		}		
	}

	// ----------------------------------------------------------------------------
	/**
	 * Decrementa o num de transmissores que estao sendo utilizados Este metodo
	 * dever ser invocado quando uma conexao com origem neste ns for finalizada
	 */
	public void freeTx() {
		this.txUtilization--;
	}

	/**
	 * restart
	 */
	public void reStart() {
		this.txUtilization = 0;
	}

	public int getTxUtilization() {
		return txUtilization;
	}

	public int getNumberOfTx() {
		return numberOfTx;
	}

	public void setNumberOfTx(int numberOfTx) {
		this.numberOfTx = numberOfTx;
	}
	
	public boolean isFullUtilized(){
		return numberOfTx==txUtilization;
	}
	

}

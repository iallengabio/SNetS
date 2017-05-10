package network;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Receivers implements Serializable {

	private int rxUtilization;
	private int numberOfRx=1000000; //sem limitação de receivers

	public Receivers() {
		this.rxUtilization = 0;
	}

	public Receivers(int numberOfRx) {
		this.numberOfRx = numberOfRx;
		this.rxUtilization = 0;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Incrementa o num de receptores que estao sendo utilizados e atualiza o
	 * somatorio e o pico de utilizacao de receptor
	 */
	public boolean alocRx() {
		if(this.rxUtilization<this.numberOfRx){
			this.rxUtilization++;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Decrementa o num de receptores que estao sendo utilizados
	 */
	public void freeRx() {
		this.rxUtilization--;
	}

	/**
	 * restart
	 */
	public void reStart() {
		this.rxUtilization = 0;
	}

	public int getRxUtilization() {
		return rxUtilization;
	}

	public void setNumberOfRx(int numberOfRx) {
		this.numberOfRx = numberOfRx;
	}
	
	public boolean isFullUtilized(){
		return numberOfRx==rxUtilization;
	}
	
	

}

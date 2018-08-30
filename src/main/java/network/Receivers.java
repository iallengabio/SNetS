package network;

import java.io.Serializable;

/**
 * This class represents the receivers on the network
 * 
 * @author Iallen
 */
public class Receivers implements Serializable {

	private int rxUtilization;
	private int numberOfRx;

	/**
	 * Creates a new instance of Receivers
	 */
	public Receivers() {
		this.rxUtilization = 0;
		this.numberOfRx = Integer.MAX_VALUE; // Without limitation of receivers
	}

	/**
	 * Creates a new instance of Receivers with a limited number of receivers
	 * 
	 * @param numberOfRx int
	 */
	public Receivers(int numberOfRx) {
		this.numberOfRx = numberOfRx;
		this.rxUtilization = 0;
	}

	/**
	 * Increases the number of receivers being used and updates the sum and peak receiver utilization
	 */
	public boolean allocatesReceivers() {
		if(this.rxUtilization < this.numberOfRx){
			this.rxUtilization++;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Decrements the number of receivers being used
	 */
	public void releasesReceivers() {
		this.rxUtilization--;
	}

	/**
	 * restart
	 */
	public void reStart() {
		this.rxUtilization = 0;
	}

	/**
	 * Returns the use of receivers
	 * 
	 * @return int
	 */
	public int getRxUtilization() {
		return rxUtilization;
	}
	
	/**
	 * Returns the maximum number of receivers
	 * 
	 * @return int
	 */
	public int getNumberOfRx() {
		return numberOfRx;
	}

	/**
	 * Sets the maximum number of receivers
	 * 
	 * @param numberOfRx int
	 */
	public void setNumberOfRx(int numberOfRx) {
		this.numberOfRx = numberOfRx;
	}
	
	/**
	 * To verify if the use of receivers has reached the maximum number of available receivers
	 * 
	 * @return boolean
	 */
	public boolean isFullUtilized(){
		return numberOfRx == rxUtilization;
	}
	
	/**
	 * Check if there are free receivers.
	 * Returns true if there are free receivers, otherwise returns false.
	 * 
	 * @return boolean
	 */
	public boolean hasFreeRecivers(){
		return (rxUtilization < numberOfRx);
	}
}

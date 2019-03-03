package network;

import java.io.Serializable;

/**
 * This class represents the transmitters on the network
 * 
 * @author Iallen
 */
public class Transmitters implements Serializable {

	private int maxSpectralAmplitude = 80;
	private int txUtilization;
	private int numberOfTx;

	/**
	 * Creates a new instance of Transmitters
	 */
	public Transmitters() {
		this.txUtilization = 0;
		this.numberOfTx = Integer.MAX_VALUE; // Without limitation of transmitters
	}

	/**
	 * Creates a new instance of Transmitters with a limited number of transmitters
	 * 
	 * @param numberOfTx int
	 */
	public Transmitters(int numberOfTx, int maxSpectralAmplitude){
		this.numberOfTx = numberOfTx;
		this.txUtilization = 0;
		this.maxSpectralAmplitude = maxSpectralAmplitude;
	}

	public int getMaxSpectralAmplitude() {
		return maxSpectralAmplitude;
	}

	public void setMaxSpectralAmplitude(int maxSpectralAmplitude) {
		this.maxSpectralAmplitude = maxSpectralAmplitude;
	}

	/**
	 * Increases the number of transmitters being used and updates the sum and peak transmitter utilization
	 */
	public boolean allocatesTransmitters() {
		if(this.txUtilization < this.numberOfTx){
			this.txUtilization++;
			return true;
		}else{
			return false;
		}		
	}

	/**
	 * Decrements the number of transmitters being used
	 * This method should be invoked when a connection originating from this node source is terminated
	 */
	public void releasesTransmitters() {
		this.txUtilization--;
	}

	/**
	 * restart
	 */
	public void reStart() {
		this.txUtilization = 0;
	}

	/**
	 * Returns the use of transmitters
	 * 
	 * @return int
	 */
	public int getTxUtilization() {
		return txUtilization;
	}

	/**
	 * Returns the maximum number of transmitters
	 * 
	 * @return int
	 */
	public int getNumberOfTx() {
		return numberOfTx;
	}

	/**
	 * Sets the maximum number of transmitters
	 * 
	 * @param numberOfTx int
	 */
	public void setNumberOfTx(int numberOfTx) {
		this.numberOfTx = numberOfTx;
	}
	
	/**
	 * To verify if the use of transmitters has reached the maximum number of available transmitters
	 * 
	 * @return boolean
	 */
	public boolean isFullUtilized(){
		return numberOfTx == txUtilization;
	}
	
	/**
	 * Check if there are free transmitters.
	 * Returns true if there are free transmitters, otherwise returns false.
	 * 
	 * @return boolean
	 */
	public boolean hasFreeTransmitters(){
		return (txUtilization < numberOfTx);
	}
}

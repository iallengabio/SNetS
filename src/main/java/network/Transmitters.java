package network;

/**
 * This class represents the transmitters on the network
 * 
 * @author Iallen
 */
public class Transmitters {

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
	public Transmitters(int numberOfTx){
		this.numberOfTx = numberOfTx;
		this.txUtilization = 0;
	}

	/**
	 * Increases the number of transmitters being used and updates the sum and peak transmitter utilization
	 *
	 * @return boolean
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
	 * Increases the number of transmitters being used and updates the sum and peak transmitter utilization
	 * 
	 * @param numberOfTxs int
	 * @return boolean
	 */
	public boolean allocatesTransmitters(int numberOfTxs) {
		if((numberOfTx - txUtilization) >= numberOfTxs){
			this.txUtilization += numberOfTxs;
			
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
	 * Decrements the number of transmitters being used
	 * This method should be invoked when a connection originating from this node source is terminated
	 * 
	 * @param numberOfTxs int
	 */
	public void releasesTransmitters(int numberOfTxs) {
		this.txUtilization -= numberOfTxs;
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
	
	/**
	 * Check if there are enough free transmitters.
	 * Returns true if there are free transmitters, otherwise returns false.
	 * 
	 * @param numberOfTransmitters int
	 * @return boolean
	 */
	public boolean hasEnoughFreeTransmitters(int numberOfTransmitters){
		return ((numberOfTx - txUtilization) >= numberOfTransmitters);
	}
}

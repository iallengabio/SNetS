package grmlsa.modulation;

import network.PhysicalLayer;

import java.io.Serializable;

/**
 * This class represents the modulation formats.
 * 
 * @author Iallen
 */
public class Modulation implements Serializable, Cloneable {

    private String name;
    private double maxRange; // max range in Km
    private double M; // Number of modulation format symbols
    private double bitsPerSymbol;
    
    private double SNRthreshold; // dB
    private double SNRthresholdLinear;
	
    private double rateFEC; // rate of Forward Error Correction
	private double freqSlot;
	private int guardBand;

	private double p; // number of polarization modes
	
	/**
	 * Creates a new instance of Modulation
	 * 
	 * @param name String
	 * @param maxRange double
	 * @param M double
	 * @param SNRthreshold double
	 * @param rateFEC double
	 * @param freqSlot double
	 * @param guardBand double
	 */
    public Modulation(String name, double maxRange, double M, double SNRthreshold, double rateFEC, double freqSlot, int guardBand, double p) {
        this.name = name;
        this.maxRange = maxRange;
        this.M = M;
        this.SNRthreshold = SNRthreshold;
        this.rateFEC = rateFEC;
        this.freqSlot = freqSlot;
        this.guardBand = guardBand;
        this.p = p;
        
        // Calculation based on article: Capacity Limits of Optical Fiber Networks (2010)
        this.bitsPerSymbol = PhysicalLayer.log2(M);
        this.SNRthresholdLinear = PhysicalLayer.ratioOfDB(SNRthreshold);
    }

    /**
     * Returns the number of slots required according to the bit rate
     * 
     * Based on articles:
     *  - Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks (2013)
	 *  - Influence of Physical Layer Configuration on Performance of Elastic Optical OFDM Networks (2014)
	 *  - Analise do Impacto do Ruido ASE em Redes Opticas Elasticas Transparentes Utilizando Multiplos Formatos de Modulacao (2015)
     *
     * @param bitRate
     * @return int - slotsNumberTemp
     */
    public int requiredSlots(double bitRate) {
    	double slotsNumber = (bitRate * (1.0 + rateFEC)) / (p * bitsPerSymbol * freqSlot);
        
        int slotsNumberTemp = (int) slotsNumber;
        if (slotsNumber - slotsNumberTemp != 0.0) {
        	slotsNumberTemp++;
        }
        
        //slotsNumberTemp = slotsNumberTemp + guardBand; // Adds another slot needed to be used as a guard band
        
        return slotsNumberTemp;
    }

    /**
     * Compute the potential bit rate when @slotsNumber slots are utilized
     * 
     * @param slotsNumber int
     * @return double
     */
    public double potentialBitRate(int slotsNumber){
    	//slotsNumber = slotsNumber - guardBand; // Remove the slot required to be used as a guard band
        
        return (slotsNumber * p * bitsPerSymbol * freqSlot) / (1.0 + rateFEC);
    }

    /**
     * Returns the bandwidth from a bit rate
     * 
     * @param bitRate
     * @return double
     */
    public double getBandwidthFromBitRate(double bitRate) {
    	double bandwidth = (bitRate * (1.0 + rateFEC)) / (p * bitsPerSymbol);
    	
    	return bandwidth;
    }
    
	/**
	 * Returns the name of the modulation
	 * 
     * @return the name - String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the bits per symbol
     * 
     * @return the bitsPerSymbol - double
     */
    public double getBitsPerSymbol() {
        return bitsPerSymbol;
    }

    /**
     * Return the maximum range
     * 
     * @return the maxRange - double
     */
    public double getMaxRange() {
        return maxRange;
    }
    
    /**
     * Sets the maximum range
     * 
     * @param maxRange - double
     */
    public void setMaxRange(double maxRange) {
    	this.maxRange = maxRange;
    }
    
    /**
     * Returns the M
     * 
     * @return double
     */
    public double getM() {
    	return M;
    }
    
    /**
     * Return the SNR threshold of the modulation
     * 
     * @return double
     */
    public double getSNRthreshold(){
    	return SNRthreshold;
    }
    
    /**
     * Returns the SNR threshold linear linear of the modulation
     * 
     * @return double
     */
    public double getSNRthresholdLinear(){
    	return SNRthresholdLinear;
    }
    
    /**
     * Returns the guard band
     * 
     * @return int
     */
    public int getGuardBand(){
    	return guardBand;
    }
    
    /**
     * Sets the guard band
     * 
     * @param guardBand int
     */
    public void setGuardBand(int guardBand) {
		this.guardBand = guardBand;
    }
    
    @Override
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
    }
}

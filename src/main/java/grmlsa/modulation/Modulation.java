package grmlsa.modulation;

import network.PhysicalLayer;

import java.io.Serializable;

/**
 * This class represents the modulation formats.
 * 
 * @author Iallen
 */
public class Modulation implements Serializable {
	
    private String name;
    private double maxRange; // max range in Km
    private double M; // Number of modulation format symbols
    private double bitsPerSymbol;
    
    private double SNRthreshold; // dB
    private double SNRthresholdLinear;
	
    private double rateFEC; // rate of Forward Error Correction
	private double freqSlot;
	private int guardBand;
	
	/**
	 * Creates a new instance of Modulation
	 * 
	 * @param name String
	 * @param freqSlot double
	 * @param maxRange double
	 * @param guardBand double
	 * @param level double
	 * @param M double
	 * @param FEC double
	 */
    public Modulation(String name, double maxRange, double M, double SNRthreshold, double rateFEC, double freqSlot, int guardBand) {
        this.name = name;
        this.maxRange = maxRange;
        this.M = M;
        this.SNRthreshold = SNRthreshold;
        this.rateFEC = rateFEC;
        this.freqSlot = freqSlot;
        this.guardBand = guardBand;
        
        // Calculation based on article: Capacity Limits of Optical Fiber Networks (2010)
        this.bitsPerSymbol = PhysicalLayer.log2(M);
        this.SNRthresholdLinear = PhysicalLayer.ratioOfDB(SNRthreshold);
    }

    /**
     * Returns the number of slots required according to the bandwidth
     * Adds guard band
     * 
     * Based on articles:
     *  - Efficient Resource Allocation for All-Optical Multicasting Over Spectrum-Sliced Elastic Optical Networks (2013)
	 *  - Influence of Physical Layer Configuration on Performance of Elastic Optical OFDM Networks (2014)
	 *  - Analise do Impacto do Ruido ASE em Redes Opticas Elasticas Transparentes Utilizando Multiplos Formatos de Modulacao (2015)
     *
     * @param bandwidth
     * @return int - numberOfStos
     */
    public int requiredSlots(double bandwidth) {
    	double slotsNumber = (bandwidth * (1.0 + rateFEC)) / (bitsPerSymbol * freqSlot);
        
        int slotsNumberTemp = (int) slotsNumber;
        if (slotsNumber - slotsNumberTemp != 0.0) {
        	slotsNumberTemp++;
        }

        slotsNumberTemp = slotsNumberTemp + guardBand; // Adds another slot needed to be used as a guard band

        return slotsNumberTemp;
    }

    /**
     * compute the potential bandwidth when @slotsNumber slots are utilized
     * @param slotsNumber
     * @return
     */
    public double potentialBandwidth(int slotsNumber){
    	slotsNumber = slotsNumber - guardBand; // Remove the slot required to be used as a guard band
        
        return (slotsNumber * bitsPerSymbol * freqSlot) / (1.0 + rateFEC);
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
    
}

package grmlsa.modulation;

import network.PhysicalLayer;

/**
 * This class represents the modulation formats.
 * 
 * @author Iallen
 */
public class Modulation {

    private String name;
    private double bitsPerSymbol;
    private double maxRange; // max range in Km
    
    private double level; // Level of modulation format
    private double M; // Number of modulation format symbols
    private double SNRthreshold; // dB
    private double SNRthresholdLinear;
	
    private double rateFEC; // Forward Error Correction
	private double freqSlot;
	private double guardBand;
	
	private boolean activeQoT;


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
    public Modulation(String name, double maxRange, double level, double M, double SNRthreshold, double rateFEC, double freqSlot, double guardBand, boolean activeQoT) {
        this.name = name;
        this.maxRange = maxRange;
        this.level = level;
        this.M = M;
        this.SNRthreshold = SNRthreshold;
        this.rateFEC = rateFEC;
        this.freqSlot = freqSlot;
        this.guardBand = guardBand;
        
        // Calculation based on article: Capacity Limits of Optical Fiber Networks (2010)
        this.bitsPerSymbol = PhysicalLayer.log2(M);
        this.SNRthresholdLinear = PhysicalLayer.ratioOfDB(SNRthreshold);
        this.activeQoT = activeQoT;
    }
    
    /**
     * This method selects how the number of slots will be calculated
     * 
     * @param bandwidth double
     * @return int
     */
    public int requiredSlots(double bandwidth){
		int res = 1;
		
		if (activeQoT) {
			res = requiredSlotsByQoT(bandwidth);
		} else {
			res = requiredSlotsWithoutFEC(bandwidth);
		}
		
		return res;
	}

    /**
     * Returns the number of slots required according to the bandwidth
     * Adds guard band
     *
     * @param bandwidth
     * @return
     */
    private int requiredSlotsWithoutFEC(double bandwidth) {
        //System.out.println("C = " + bandwidth + "    bm = " + this.bitsPerSimbol + "     fslot = " + this.freqSlot);

        double res = bandwidth / (this.bitsPerSymbol * this.freqSlot);

        //System.out.println("res = " + res);

        int res2 = (int) res;

        if (res - res2 != 0.0) {
            res2++;
        }

        res2 = res2 + (int)guardBand; //Adds another slot needed to be used as a guard band

        return res2;
    }
    
    /**
	 * Based on article:
	 *  - Influence of Physical Layer Configuration on Performance of Elastic Optical OFDM Networks (2014)
	 * 
	 * @param bandwidth double
	 * @return int
	 */
	private int requiredSlotsByQoT(double bandwidth){
		double Bn = bandwidth; //(bandwidth / 1073741824.0) * 1.0E+9;
		double Bs = (1.1 * Bn * (1 + rateFEC)) / (2 * PhysicalLayer.log2(this.level)); //single channel bandwidth, Hz
		double deltaG = 2.0 * guardBand; //guard band between adjacent spectrum (Obs.: A guard band for each edge of the required bandwidth)
		double deltaB = Bs + deltaG; //channel spacing
		
		double res = deltaB / this.freqSlot;
		int res2 = (int)res;
		if(res - res2 != 0.0){
			res2++;
		}
		
		return res2;
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
     * Returns the level
     * 
     * @return double
     */
    public double getLevel() {
    	return level;
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
}

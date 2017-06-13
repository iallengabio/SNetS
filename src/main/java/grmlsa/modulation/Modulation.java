package grmlsa.modulation;

import network.Mesh;
import network.PhysicalLayer;

/**
 * This class represents the modulation formats.
 * 
 * @author Iallen
 */
public class Modulation {

    private final int guardBand;
    private String name;
    private double bitsPerSymbol;
    /*
     * max range in Km
     */
    private double maxRange;

    private double freqSlot;
    
    private double level; //Level of modulation format
	public double k2;
	public double M; //Number of modulation format symbols

	private Mesh mesh;

	/**
	 * Creates a new instance of Modulation
	 * 
	 * @param name String
	 * @param bitsPerSymbol double
	 * @param freqSlot double
	 * @param maxRange double
	 * @param guardBand int
	 * @param level double
	 * @param k2 double
	 * @param M double
	 * @param mesh Mesh
	 */
    public Modulation(String name, double bitsPerSymbol, double freqSlot, double maxRange, int guardBand, double level, double k2, double M, Mesh mesh) {
        this.name = name;
        this.bitsPerSymbol = bitsPerSymbol;
        this.maxRange = maxRange;
        this.freqSlot = freqSlot;
        this.guardBand = guardBand;
        this.level = level;
        this.k2 = k2;
        this.M = M;
        
        this.mesh = mesh;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the bitsPerSymbol
     */
    public double getBitsPerSymbol() {
        return bitsPerSymbol;
    }

    /**
     * @return the maxRange
     */
    public double getMaxRange() {
        return maxRange;
    }
    
    public int requiredSlots(double bandwidth){
		int res = 1;
		
		if (mesh.getPhysicalLayer().isActiveQoT()) {
			res = requiredSlotsByQoT(bandwidth);
		} else {
			res = requiredSlots1(bandwidth);
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
    private int requiredSlots1(double bandwidth) {
        //System.out.println("C = " + bandwidth + "    bm = " + this.bitsPerSimbol + "     fslot = " + this.freqSlot);

        double res = bandwidth / (this.bitsPerSymbol * this.freqSlot);

        //System.out.println("res = " + res);

        int res2 = (int) res;

        if (res - res2 != 0.0) {
            res2++;
        }

        res2 = res2 + guardBand; //Adds another slot needed to be used as a guard band

        return res2;
    }
    
    /**
	 * Based on article: Influence of Physical Layer Configuration on Performance of 
	 * Elastic Optical OFDM Networks (2014)
	 * 
	 * @param bandwidth double
	 * @return int
	 */
	private int requiredSlotsByQoT(double bandwidth){
		double F = mesh.getPhysicalLayer().getFEC();
		double Bn = bandwidth; //(bandwidth / 1073741824.0) * 1.0E+9;
		double Bs = (1.1 * Bn * (1 + F)) / (2 * PhysicalLayer.log2(this.level)); //single channel bandwidth, Hz
		double deltaG = 2.0 * mesh.getPhysicalLayer().getGuardBand(); //guard band between adjacent spectrum (Obs.: A guard band for each edge of the required bandwidth)
		double deltaB = Bs + deltaG; //channel spacing
		
		double res = deltaB / this.freqSlot;
		int res2 = (int)res;
		if(res - res2 != 0.0){
			res2++;
		}
		
		return res2;
	}

	/**
	 * @return the level
	 */
	public double getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(double level) {
		this.level = level;
	}

	/**
	 * @return the k2
	 */
	public double getK2() {
		return k2;
	}

	/**
	 * @param k2 the k2 to set
	 */
	public void setK2(double k2) {
		this.k2 = k2;
	}

	/**
	 * @return the m
	 */
	public double getM() {
		return M;
	}

	/**
	 * @param m the m to set
	 */
	public void setM(double m) {
		M = m;
	}
    
}

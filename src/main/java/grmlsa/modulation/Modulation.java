package grmlsa.modulation;

public class Modulation {

    private final int guardBand;
    private String name;

    private double bitsPerSymbol;
    /*
     * max range in Km
     */
    private double maxRange;

    private double freqSlot;
    
    private double level; //nivel do formato de modulacao
	public double k2;
	public double M; //quantidade de simbolos do formato de modulacao


    public Modulation(String name, double bitsPerSymbol, double freqSlot, double maxRange, int guardBand) {
        this.name = name;
        this.bitsPerSymbol = bitsPerSymbol;
        this.maxRange = maxRange;
        this.freqSlot = freqSlot;
        this.guardBand = guardBand;
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

    /**
     * retorna a quantidade de slots necessï¿½rios de acordo com a largura de banda
     * adiciona a banda de guarda
     *
     * @param bandwidth
     * @return
     */
    public int requiredSlots(double bandwidth) {
        //System.out.println("C = " + bandwidth + "    bm = " + this.bitsPerSimbol + "     fslot = " + this.freqSlot);

        double res = bandwidth / (this.bitsPerSymbol * this.freqSlot);

        //System.out.println("res = " + res);

        int res2 = (int) res;

        if (res - res2 != 0.0) {
            res2++;
        }

        res2 = res2 + guardBand; //adiciona mais um slot necessário para ser usado como banda de guarda

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

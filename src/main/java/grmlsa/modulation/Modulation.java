package grmlsa.modulation;

public class Modulation {

    private final int guardBand;
    private String name;

    private double bitsPerSimbol;
    /*
     * max range in Km
     */
    private double maxRange;

    private double freqSlot;


    public Modulation(String name, double bitsPerSimbol, double freq, double maxRange, int guardBand) {
        this.name = name;
        this.bitsPerSimbol = bitsPerSimbol;
        this.maxRange = maxRange;
        this.freqSlot = freq;
        this.guardBand = guardBand;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the bitsPerSimbol
     */
    public double getBitsPerSimbol() {
        return bitsPerSimbol;
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

        double res = bandwidth / (this.bitsPerSimbol * this.freqSlot);

        //System.out.println("res = " + res);

        int res2 = (int) res;

        if (res - res2 != 0.0) {
            res2++;
        }

        res2 = res2 + guardBand; //adiciona mais um slot necessário para ser usado como banda de guarda


        return res2;
    }


}

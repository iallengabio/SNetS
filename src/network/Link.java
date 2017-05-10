package network;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Link implements Serializable {

    private Oxc source;
    private Oxc destination;
    private double cost;
    private Spectrum spectrum;
    private double distance;

    /**
     * Creates a new instance of Link.
     *
     * @param s             Oxc New value of property source.
     * @param d             Oxc New value of property destination.
     * @param numberOfSlots int New value of property numberWave
     * @param distance      double
     */
    public Link(Oxc s, Oxc d, int numberOfSlots, double spectrumBand, double distance) {
        this.source = s;
        this.destination = d;
        this.spectrum = new Spectrum(numberOfSlots, spectrumBand);
        this.distance = distance;
    }


//------------------------------------------------------------------------------

    /**
     * is node x destination of this link.
     *
     * @param x Oxc
     * @return true if Oxc x is destination of this Link; false otherwise.
     */
    public boolean adjacent(Oxc x) {
        if (destination == x) {
            return true;
        } else {
            return false;
        }
    }

//------------------------------------------------------------------------------


    /**
     * Este método ocupa uma determinada faixa de spectro definida no parâmetro
     *
     * @param interval vetor de duas posições, a primeira se refere ao primeiro slot e a segunda ao último slot a ser utilizado
     * @return
     */
    public boolean useSpectrum(int interval[]) {
        return spectrum.useSpectrum(interval);
    }


//------------------------------------------------------------------------------

    /**
     * Libera uma determinada faixa de spectro que está sendo utilizada
     *
     * @param spectrumBand
     */
    public void liberateSpectrum(int spectrumBand[]) {
        spectrum.freeSpectrum(spectrumBand);
    }


//------------------------------------------------------------------------------

    /**
     * Getter for property destination.
     *
     * @return Oxc destination
     */
    public Oxc getDestination() {
        return destination;
    }

//------------------------------------------------------------------------------

    /**
     * Setter for property destination.
     *
     * @param destination Oxc New value of property destination.
     */
    public void setDestination(Oxc destination) {
        this.destination = destination;
    }

//------------------------------------------------------------------------------

    /**
     * Setter for property source.
     *
     * @param source Oxc New value of property source.
     */
    public void setSource(Oxc source) {
        this.source = source;
    }
    //------------------------------------------------------------------------------

    /**
     * Getter for property source.
     *
     * @return Oxc source
     */
    public Oxc getSource() {
        return source;
    }
    //------------------------------------------------------------------------------

    /**
     * Getter for property cost.
     *
     * @return double cost
     */
    public double getCost() {
        return cost;
    }
    //------------------------------------------------------------------------------

    /**
     * Setter for property Cost.
     *
     * @param cost double new cost.
     */
    public void setCost(double cost) {
        this.cost = cost;
    }
    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------

    /**
     * Retorna a distancia deste link
     *
     * @return double
     */
    public double getDistance() {
        return distance;
    }

    //------------------------------------------------------------------------------

    /**
     * retorna o nome do link no formato <origem, destino>
     *
     * @return String
     */
    public String getName() {
        return "<" + getSource().getName() + "," + getDestination().getName() + ">";
    }
    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------

    /**
     * para reiniciar o calculo de metricas
     */
    public void reStart() {
    }

    /**
     * Retorna a lista de faixas de espectro disponíveis no link
     *
     * @return
     */
    public List<int[]> getFreeSpectrumBands() {
        return spectrum.getFreeSpectrumBands();
    }

    /**
     * @return the slotSpectrumBand
     */
    public double getSlotSpectrumBand() {
        return spectrum.getSlotSpectrumBand();
    }

    /**
     * @return the numOfSlots
     */
    public int getNumOfSlots() {
        return spectrum.getNumOfSlots();
    }


    /**
     * retorna a utilização do link
     *
     * @return
     */
    public Double getUtilization() {
        return this.spectrum.utilization();
    }

}

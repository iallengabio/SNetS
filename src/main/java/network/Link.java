package network;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;


/**
 * This class represents a network link
 * 
 * @author Iallen
 */
public class Link implements Serializable {

    private Oxc source;
    private Oxc destination;
    private double cost;
    private Spectrum spectrum;
    private double distance;
    
    private TreeSet<Circuit> circuitList;

    /**
     * Creates a new instance of Link.
     *
     * @param s             Oxc New value of property source.
     * @param d             Oxc New value of property destination.
     * @param numberOfSlots int New value of property number of slots
     * @param spectrumBand  double New value of property spectrum band
     * @param distance      double New Value of distance
     */
    public Link(Oxc s, Oxc d, int numberOfSlots, double spectrumBand, double distance) {
        this.source = s;
        this.destination = d;
        this.spectrum = new Spectrum(numberOfSlots, spectrumBand);
        this.distance = distance;
        
        this.circuitList = new TreeSet<Circuit>();
    }

    /**
     * Is node x destination of this link.
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

    /**
     * This method occupies a certain range of spectrum defined in the parameter
     *
     * @param interval - int[] - Vector of two positions, the first refers to the first slot 
     *                           and the second to the last slot to be used
     * @return boolean
     */
    public boolean useSpectrum(int interval[]) throws Exception {
        return spectrum.useSpectrum(interval);
    }

    /**
     * Releases a certain range of spectrum being used
     *
     * @param spectrumBand int[]
     */
    public void liberateSpectrum(int spectrumBand[]) throws Exception {
        spectrum.freeSpectrum(spectrumBand);
    }

    /**
     * Getter for property destination.
     *
     * @return Oxc destination
     */
    public Oxc getDestination() {
        return destination;
    }

    /**
     * Setter for property destination.
     *
     * @param destination Oxc New value of property destination.
     */
    public void setDestination(Oxc destination) {
        this.destination = destination;
    }

    /**
     * Setter for property source.
     *
     * @param source Oxc New value of property source.
     */
    public void setSource(Oxc source) {
        this.source = source;
    }

    /**
     * Getter for property source.
     *
     * @return Oxc source
     */
    public Oxc getSource() {
        return source;
    }

    /**
     * Getter for property cost.
     *
     * @return double cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Setter for property Cost.
     *
     * @param cost double new cost.
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Returns the distance of this link
     *
     * @return double
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the name of the link in the format <source, destination>
     *
     * @return String
     */
    public String getName() {
        return "<" + getSource().getName() + "," + getDestination().getName() + ">";
    }

    /**
     * Returns the list of spectrum bands available on the link
     *
     * @return List<int[]>
     */
    public List<int[]> getFreeSpectrumBands() {
        return spectrum.getFreeSpectrumBands();
    }

    /**
     * Returns the bandwidth of a slot
     * 
     * @return the slotSpectrumBand
     */
    public double getSlotSpectrumBand() {
        return spectrum.getSlotSpectrumBand();
    }

    /**
     * Returns the number of slots in the link
     * 
     * @return int the numOfSlots
     */
    public int getNumOfSlots() {
        return spectrum.getNumOfSlots();
    }
    
    /**
     * Returns the number of used slots
     * 
	 * @return int
	 */
	public int getUsedSlots(){
		return spectrum.getUsedSlots();
	}

    /**
     * Returns link usage
     *
     * @return Double
     */
    public Double getUtilization() {
        return this.spectrum.utilization();
    }
	
	/**
	 * Returns the list of circuits that use this link
	 * 
	 * @return the listRequests
	 */
	public TreeSet<Circuit> getCircuitList() {
		return circuitList;
	}

	/**
	 * Sets the list of circuits that use this link
	 * 
	 * @param listRequests the listRequests to set
	 */
	public void setCircuitList(TreeSet<Circuit> circuitList) {
		this.circuitList = circuitList;
	}
	
	/**
	 * Adds a circuit to the list of circuits that use this link
	 * 
	 * @param circuit Circuit
	 */
	public void addCircuit(Circuit circuit){
		if(!circuitList.contains(circuit)){
			circuitList.add(circuit);
		}
	}
	
	/**
	 * Removes a circuit from the list of circuits using this link
	 * 
	 * @param circuit Circuit
	 */
	public void removeCircuit(Circuit circuit){
		circuitList.remove(circuit);
	}
	
}

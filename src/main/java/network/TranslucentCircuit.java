package network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import grmlsa.modulation.Modulation;

/**
 * This class represents an established translucent circuit in an optical network
 * 
 * @author Alexandre
 */
public class TranslucentCircuit extends Circuit implements Serializable {
	
	private HashMap<Link, int[]> spectrumAssignedByLink;
	protected HashMap<Link, Modulation> modulationByLink;
	
	// list of the indexes of the ones that regenerated the signal and the noise
    protected List<Integer> regeneratorsNodesIndexList;

	public TranslucentCircuit() {
		super();
		
		this.spectrumAssignedByLink = new HashMap<>();
		this.modulationByLink = new HashMap<>();
	}

	/**
	 * @return the spectrumAssignedByLink
	 */
	public HashMap<Link, int[]> getSpectrumAssignedByLink() {
		return spectrumAssignedByLink;
	}

	/**
	 * @param spectrumAssignedByLink the spectrumAssignedByLink to set
	 */
	public void setSpectrumAssignedByLink(HashMap<Link, int[]> spectrumAssignedByLink) {
		this.spectrumAssignedByLink = spectrumAssignedByLink;
	}

	/**
	 * @return the modulationByLink
	 */
	public HashMap<Link, Modulation> getModulationByLink() {
		return modulationByLink;
	}

	/**
	 * @param modulationByLink the modulationByLink to set
	 */
	public void setModulationByLink(HashMap<Link, Modulation> modulationByLink) {
		this.modulationByLink = modulationByLink;
	}

	/**
	 * @return the regeneratorsNodesIndexList
	 */
	public List<Integer> getRegeneratorsNodesIndexList() {
		return regeneratorsNodesIndexList;
	}

	/**
	 * @param regeneratorsNodesIndexList the regeneratorsNodesIndexList to set
	 */
	public void setRegeneratorsNodesIndexList(List<Integer> regeneratorsNodesIndexList) {
		this.regeneratorsNodesIndexList = regeneratorsNodesIndexList;
	}
	
	/**
	 * This method returns the spectrum allocated by the circuit on a link
     * Can change according to the type of circuit
	 * 
	 * @param link - Link
	 * @return int[]
	 */
	public int[] getSpectrumAssignedByLink(Link link){
		int sa[] = getSpectrumAssignedByLink().get(link);
		return sa;
	}
	
	/**
	 * This method that returns the modulation format used in a given route link
	 * Can change according to the type of circuit
	 * 
	 * @param link - Link
	 * @return Modulation
	 */
	public Modulation getModulationByLink(Link link){
		Modulation mod = getModulationByLink().get(link);
		return mod;
	}
}

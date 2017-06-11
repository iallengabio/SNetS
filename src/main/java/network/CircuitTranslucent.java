package network;

import java.util.HashMap;
import java.util.List;

import grmlsa.modulation.Modulation;


public class CircuitTranslucent extends Circuit {
	
	private HashMap<Link, int[]> spectrumAssignedByLink;
	protected HashMap<Link, Modulation> modulationByLink;
	
	// list of the indices of the ones that regenerated the signal and the noise
    protected List<Integer> regeneratorsNodesIndexList;

	public CircuitTranslucent() {
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
	
}

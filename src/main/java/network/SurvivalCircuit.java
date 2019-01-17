package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;

/**
 * This class represents an optical circuit with ability to survive failures.
 * 
 * @author Alexandre
 */
public class SurvivalCircuit extends Circuit {
	
	// To verify that the primary route is in use
	protected boolean isPrimaryRoute;
	
	// Backup route
	protected Route backupRoute;
	
	// Spectrum assigned by backup route
	protected int[] spectrumAssignedByBackupRoute;
	
	// Modulation by backup route
	protected Modulation modulationByBackupRoute;
	
	protected int requiredNumberOfTxs;
	protected int requiredNumberOfRxs;
	
	/**
	 * Creates a new instance of SurvivalCircuit.
	 */
	public SurvivalCircuit() {
		super();
		
		this.isPrimaryRoute = true;
		
		this.requiredNumberOfTxs = 1;
		this.requiredNumberOfRxs = 1;
	}

	/**
	 * @return the isPrimaryRoute
	 */
	public boolean isPrimaryRoute() {
		return isPrimaryRoute;
	}

	/**
	 * @param isPrimaryRoute the isPrimaryRoute to set
	 */
	public void setPrimaryRoute(boolean isPrimaryRoute) {
		this.isPrimaryRoute = isPrimaryRoute;
	}

	/**
	 * @return the backupRoute
	 */
	public Route getBackupRoute() {
		return backupRoute;
	}

	/**
	 * @param backupRoute the backupRoute to set
	 */
	public void setBackupRoute(Route backupRoute) {
		this.backupRoute = backupRoute;
	}

	/**
	 * @return the spectrumAssignedByBackupRoute
	 */
	public int[] getSpectrumAssignedByBackupRoute() {
		return spectrumAssignedByBackupRoute;
	}

	/**
	 * @param spectrumAssignedByBackupRoute the spectrumAssignedByBackupRoute to set
	 */
	public void setSpectrumAssignedByBackupRoute(int[] spectrumAssignedByBackupRoute) {
		this.spectrumAssignedByBackupRoute = spectrumAssignedByBackupRoute;
	}

	/**
	 * @return the modulationByBackupRoute
	 */
	public Modulation getModulationByBackupRoute() {
		return modulationByBackupRoute;
	}

	/**
	 * @param modulationByBackupRoute the modulationByBackupRoute to set
	 */
	public void setModulationByBackupRoute(Modulation modulationByBackupRoute) {
		this.modulationByBackupRoute = modulationByBackupRoute;
	}

	/**
	 * @return the requiredNumberOfTxs
	 */
	public int getRequiredNumberOfTxs() {
		return requiredNumberOfTxs;
	}

	/**
	 * @param requiredNumberOfTxs the requiredNumberOfTxs to set
	 */
	public void setRequiredNumberOfTxs(int requiredNumberOfTxs) {
		this.requiredNumberOfTxs = requiredNumberOfTxs;
	}

	/**
	 * @return the requiredNumberOfRxs
	 */
	public int getRequiredNumberOfRxs() {
		return requiredNumberOfRxs;
	}

	/**
	 * @param requiredNumberOfRxs the requiredNumberOfRxs to set
	 */
	public void setRequiredNumberOfRxs(int requiredNumberOfRxs) {
		this.requiredNumberOfRxs = requiredNumberOfRxs;
	}
	
	@Override
	public int[] getSpectrumAssignedByLink(Link link){
		int sa[] = null;
		
		if(route.containThisLink(link)){
			sa = getSpectrumAssigned();
			
		}else if(backupRoute.containThisLink(link)){
			sa = spectrumAssignedByBackupRoute;
		}
		
		return sa;
	}
	
	@Override
	public Modulation getModulationByLink(Link link){
		Modulation mod = null;
		
		if(route.containThisLink(link)){
			mod = getModulation();
			
		}else if(backupRoute.containThisLink(link)){
			mod = modulationByBackupRoute;
		}
		
		return mod;
	}
	
}

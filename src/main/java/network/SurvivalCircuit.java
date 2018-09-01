package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	// Index of the backup route in use
	protected int indexBackupRoute;
	
	// List of backup routes
	protected List<Route> backupRoutes;
	
	// Spectrum assigned by backup route
	protected HashMap<Route, int[]> spectrumAssignedByBackupRoute;
	
	// Modulation by backup route
	protected HashMap<Route, Modulation> modulationByBackupRoute;
	
	/**
	 * Creates a new instance of SurvivalCircuit.
	 */
	public SurvivalCircuit() {
		super();
		
		this.isPrimaryRoute = true;
		this.indexBackupRoute = -1;
		
		this.backupRoutes = new ArrayList<>();
		this.spectrumAssignedByBackupRoute = new HashMap<>();
		this.modulationByBackupRoute = new HashMap<>();
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
	 * @return the indexBackupRoute
	 */
	public int getIndexBackupRoute() {
		return indexBackupRoute;
	}

	/**
	 * @param indexBackupRoute the indexBackupRoute to set
	 */
	public void setIndexBackupRoute(int indexBackupRoute) {
		this.indexBackupRoute = indexBackupRoute;
	}

	/**
	 * @return the backupRoutes
	 */
	public List<Route> getBackupRoutes() {
		return backupRoutes;
	}

	/**
	 * @param backupRoutes the backupRoutes to set
	 */
	public void setBackupRoutes(List<Route> backupRoutes) {
		this.backupRoutes = backupRoutes;
	}

	/**
	 * @return the spectrumAssignedByBackupRoute
	 */
	public HashMap<Route, int[]> getSpectrumAssignedByBackupRoute() {
		return spectrumAssignedByBackupRoute;
	}

	/**
	 * @param spectrumAssignedByBackupRoute the spectrumAssignedByBackupRoute to set
	 */
	public void setSpectrumAssignedByBackupRoute(HashMap<Route, int[]> spectrumAssignedByBackupRoute) {
		this.spectrumAssignedByBackupRoute = spectrumAssignedByBackupRoute;
	}

	/**
	 * @return the modulationByBackupRoute
	 */
	public HashMap<Route, Modulation> getModulationByBackupRoute() {
		return modulationByBackupRoute;
	}

	/**
	 * @param modulationByBackupRoute the modulationByBackupRoute to set
	 */
	public void setModulationByBackupRoute(HashMap<Route, Modulation> modulationByBackupRoute) {
		this.modulationByBackupRoute = modulationByBackupRoute;
	}
	
	
	
}

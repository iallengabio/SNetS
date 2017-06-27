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
	
	
	
}

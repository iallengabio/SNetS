package grmlsa.integrated;

import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * RMLSA integrated algorithm that reads the spectrum assignment of the simulation configuration file
 * 
 * @author Alexandre
 *
 */
public class KShortestPathsAndSpectrumAssignment implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    int cont = 0;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = null;
        
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();

        // to avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
            for(Modulation mod : avaliableModulations){
            	
            	// modifica o valor da banda de guarda e cria uma copia da modulacao para testar
            	Modulation modTest = null;
            	try {
					modTest = (Modulation) mod.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
            	
            	if (circuit.getRequiredBandwidth() == 100E+9) {
            		modTest.setGuardBand(1);
            	}else if (circuit.getRequiredBandwidth() == 150E+9) {
            		modTest.setGuardBand(2);
            	}else if (circuit.getRequiredBandwidth() == 200E+9) {
            		modTest.setGuardBand(3);
            	}else if (circuit.getRequiredBandwidth() == 250E+9) {
            		modTest.setGuardBand(4);
            	}
            	
            	circuit.setModulation(modTest);
            	
            	int slotsNumber = modTest.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
	            
	            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
	            circuit.setSpectrumAssigned(band);
	            
	            if (band != null) {
	            	if(checkRoute == null){
	            		checkRoute = route;
	            		checkMod = modTest;
	            		checkBand = band;
	            	}
	            	
	            	if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modTest, band, null, false)){ //modulation has acceptable QoT
	            		chosenRoute = route;
	            		chosenBand = band;
		                chosenMod = modTest;
	            	}
	            }
            }
            
            if(chosenBand != null){
            	break;
            }
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
        	circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            
//            cont++;
//            if(cont > 50) {
//            	System.out.println("teste");
//            }

            return true;

        } else {
        	if(checkRoute == null){
				checkRoute = candidateRoutes.get(0);
				checkMod = avaliableModulations.get(0);
			}
            circuit.setRoute(checkRoute);
            circuit.setModulation(checkMod);
            circuit.setSpectrumAssigned(checkBand);

            return false;
        }
    }
    
    /**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return kShortestsPaths;
    }
}

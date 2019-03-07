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
 * Implementation based on the K Shortest Paths Computation (KS-PC) algorithm presented in:
 * - A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment Scheme for Future Elastic Optical Networks (2013)
 * 
 * @author Alexandre
 */
public class KShortestPathsComputation implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
	private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;

    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
        }
        if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection(); // Uses the modulation selection algorithm defined in the simulation file
        }
        if(spectrumAssignment == null){
			spectrumAssignment = cp.getSpectrumAssignment(); // Uses the spectrum assignment algorithm defined in the simulation file
		}

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = null;
        double chosenSNR = 0.0;
		double chosenTransmissionDistance = Double.MAX_VALUE;
		double chosenUnoccupiedSpectrum = 0.0;

        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            circuit.setModulation(mod);
            
            if(mod != null){
            	int requeridSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
	            int band[] = spectrumAssignment.policy(requeridSlots, merge, circuit, cp);
	
	            if (band != null) {
	            	circuit.setSpectrumAssigned(band);
					cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null);
					
					double SNR = circuit.getSNR();
					double transmissionDistance = route.getDistanceAllLinks();
					double unoccupiedSpectrum = sumUnoccupiedSpectrum(merge);
					
					if(SNR > chosenSNR){
						chosenBand = band;
						chosenRoute = route;
						chosenMod = mod;
						chosenSNR = SNR;
						chosenTransmissionDistance = transmissionDistance;
						chosenUnoccupiedSpectrum = unoccupiedSpectrum;
						
					}else if((SNR == chosenSNR) && (mod.getSNRthreshold() > chosenMod.getSNRthreshold())){
						chosenBand = band;
						chosenRoute = route;
						chosenMod = mod;
						chosenSNR = SNR;
						chosenTransmissionDistance = transmissionDistance;
						chosenUnoccupiedSpectrum = unoccupiedSpectrum;
						
					}else if((SNR == chosenSNR) && (mod.getSNRthreshold() == chosenMod.getSNRthreshold()) && 
							(transmissionDistance < chosenTransmissionDistance)){
						chosenBand = band;
						chosenRoute = route;
						chosenMod = mod;
						chosenSNR = SNR;
						chosenTransmissionDistance = transmissionDistance;
						chosenUnoccupiedSpectrum = unoccupiedSpectrum;
						
					}else if((SNR == chosenSNR) && (mod.getSNRthreshold() == chosenMod.getSNRthreshold()) && 
							(transmissionDistance == chosenTransmissionDistance) && (unoccupiedSpectrum > chosenUnoccupiedSpectrum)){
						chosenBand = band;
						chosenRoute = route;
						chosenMod = mod;
						chosenSNR = SNR;
						chosenTransmissionDistance = transmissionDistance;
						chosenUnoccupiedSpectrum = unoccupiedSpectrum;
					}
	            }
            }
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);

            return true;

        } else {
            circuit.setRoute(candidateRoutes.get(0));
            circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
            circuit.setSpectrumAssigned(null);
            
            return false;
        }
    }
    
    /**
     * Returns the sum of the unoccupied spectrum
     * 
     * @param spectrumFree
     * @return double
     */
    public double sumUnoccupiedSpectrum(List<int[]> spectrumFree){
		double contSpectrum = 0.0;
		for(int[] band : spectrumFree){
			contSpectrum += band[1] - band[0] + 1;
		}
		return contSpectrum;
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

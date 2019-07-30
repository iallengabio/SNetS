package grmlsa.modulation;

import java.util.List;

import grmlsa.Route;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;


/**
 * This class implements the choice of modulation and guard band using fuzzy logic.
 * 
 * @author Alexandre, Neclyeux
 *
 */
public class ModulationSelectionByQotAndFuzzy implements ModulationSelectionAlgorithmInterface {

	private List<Modulation> avaliableModulations;
	static String filename = "simulations/SPAN80/ADAPTATIVOS/Cost239_v3_MOD_ADAPTATIVA/tipper.fcl";

	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		Route checkRoute = null;
		Route chosenRoute = null;
		
		Modulation checkMod = null;
		Modulation chosenMod = null;
		
		int checkBand[] = null;
		int chosenBand[] = { 999999, 999999 }; // Value never reached
		
		if (avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}

		FIS fis = FIS.load(filename, true);

		if (fis == null) {
			System.err.println("Can't load file '" + filename + "'");
			System.exit(1);
		}

		double GuardBand = 0;

		FunctionBlock fb = fis.getFunctionBlock(null);

		circuit.setRoute(route);

		for (int m = avaliableModulations.size() - 1; m >= 0; m--) {
			Modulation mod = avaliableModulations.get(m);

			fb.setVariable("utilizacaoEnlace", maxUtilizationRouteLink(route));
			fb.setVariable("eficienciaEspectral", mod.getM());

			fb.evaluate();
			fb.getVariable("bandaGuarda").defuzzify();

			GuardBand = fb.getVariable("bandaGuarda").getValue();

			Modulation modClone = null;

			try {
				modClone = (Modulation) mod.clone();
				modClone.setGuardBand((int) GuardBand);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			circuit.setModulation(modClone);

			int slotsNumber = modClone.requiredSlots(circuit.getRequiredBandwidth());
			List<int[]> merge = IntersectionFreeSpectrum.merge(route, modClone.getGuardBand());

			int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
			circuit.setSpectrumAssigned(band);

			if (band != null && band[0] < chosenBand[0]) {
				checkRoute = route;
				checkMod = modClone;
				checkBand = band;

				if (cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, band, null, false)) { // modulation has acceptable QoT
					chosenRoute = route;
					chosenBand = band;
					chosenMod = modClone;
				}
			}
		}
		
		if(chosenMod == null){ // QoT is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
		}
		
		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		
		return chosenMod;

	}

	/**
     * Returns the maximum utilization found on the links of a route.
     * 
     * @param rota
     * @return maxUtilizationRouteLink
     */
    private double maxUtilizationRouteLink (Route route) {
    	
    	double maxUtilizationRouteLink = 0.0;
    	
    	for(Link link: route.getLinkList()) {
    		if(link.getUtilization() > maxUtilizationRouteLink) {
    			maxUtilizationRouteLink = link.getUtilization();
    		}
    	}
    	
    	return maxUtilizationRouteLink;
    }
}

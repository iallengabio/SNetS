package grmlsa.guardBand;

import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the implementation of an adaptive guard band algorithm
 * using Fuzzy Logic.
 * 
 * V2 uses a separate modulation selection and adaptive guard band algorithm.
 * 
 * Under construction.
 * 
 * @author Neclyeux, Alexandre
 */
public class GuardBandAdaptiveFuzzy2 implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; // This algorithm uses 3 alternative paths
	private KRoutingAlgorithmInterface kShortestsPaths;
	private ModulationSelectionAlgorithmInterface modulationSelection;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
	public static double maxValFuzzy = 0;
	public static double menorValFuzzy = 999;
	public static int menorM = 999;
	public static int maiorM = 0;
	public static double maxUtilizacaoRede = 0;
	public static double maxUtilizacaoRota = 0;
	public static double maxUtilizacaoEnlaceRota = 0;
	public static long tempoInicio = System.currentTimeMillis();
	private static final String DIV = "-";

	@Override
	public boolean rsa(Circuit circuit, ControlPlane cp) {
		if (kShortestsPaths == null) {
			kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); // This algorithm uses 3 alternative paths
		}
		if (modulationSelection == null) {
			modulationSelection = cp.getModulationSelection();
		}
		if (spectrumAssignment == null) {
			spectrumAssignment = new FirstFit();
		}

		List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
		Route chosenRoute = null;
		Modulation chosenMod = null;
		int chosenBand[] = { 999999, 999999 }; // Value never reached
		int checkBand[] = null;
		List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
		Route checkRoute = null;
		Modulation checkMod = null;

		for (Route route : candidateRoutes) {
			circuit.setRoute(route);

			Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);

			Modulation modClone = null;

			try {
				modClone = (Modulation) mod.clone();
				modClone.setGuardBand((int) mod.getGuardBand());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			circuit.setModulation(modClone);
			
			if (circuit.getSpectrumAssigned() != null && circuit.getSpectrumAssigned()[0] < chosenBand[0]) {
				checkRoute = route;
				checkMod = modClone;
				checkBand = circuit.getSpectrumAssigned();

				if (cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, circuit.getSpectrumAssigned(), null, false)) { // modulation has acceptable QoT
					chosenRoute = route;
					chosenBand = circuit.getSpectrumAssigned();
					chosenMod = modClone;
				}
			}
		}

		if (chosenRoute != null) { // If there is no route chosen is why no available resource was found on any of the candidate routes
			circuit.setRoute(chosenRoute);
			circuit.setModulation(chosenMod);
			circuit.setSpectrumAssigned(chosenBand);

			return true;

		} else {
			if (checkRoute == null) {
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
	@Override
	public KRoutingAlgorithmInterface getRoutingAlgorithm() {
		return kShortestsPaths;
	}

}

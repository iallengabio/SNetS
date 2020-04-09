package grmlsa;

import grmlsa.integrated.*;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistance;
import grmlsa.modulation.ModulationSelectionByDistance2;
import grmlsa.modulation.ModulationSelectionByDistanceAndBandwidth;
import grmlsa.modulation.ModulationSelectionByQoT;
import grmlsa.modulation.ModulationSelectionByQoTAndSigma;
import grmlsa.modulation.ModulationSelectionByQoTv2;
import grmlsa.regeneratorAssignment.AllAssignmentOfRegenerator;
import grmlsa.regeneratorAssignment.FLRRegeneratorAssignment;
import grmlsa.regeneratorAssignment.FNSRegeneratorAssignment;
import grmlsa.regeneratorAssignment.RegeneratorAssignmentAlgorithmInterface;
import grmlsa.routing.DJK;
import grmlsa.routing.FixedRoutes;
import grmlsa.routing.MMRDS;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.BestFit;
import grmlsa.spectrumAssignment.DispersionAdaptiveFirstLastFit;
import grmlsa.spectrumAssignment.ExactFit;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.FirstLastExactFit;
import grmlsa.spectrumAssignment.FirstLastFit;
import grmlsa.spectrumAssignment.LastFit;
import grmlsa.spectrumAssignment.RandomFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentWithInterferenceReduction;
import grmlsa.spectrumAssignment.TrafficBalancingSpectrumAssignment;
import grmlsa.spectrumAssignment.WorstFit;
import grmlsa.trafficGrooming.*;

import java.io.Serializable;

/**
 * This class should be responsible for running the RSA algorithms, verifying whether the selected 
 * algorithm is of the integrated or sequential type, after activating the algorithm (s) necessary 
 * to allocate the resource to the request
 *
 * @author Iallen
 */
public class GRMLSA implements Serializable {
	
	// Network type
	public static final int TRANSPARENT = 0;
	public static final int TRANSLUCENT = 1;
	
	// Constants that indicate which type are the RSA algorithms (sequential or integrated)
    public static final int RSA_SEQUENCIAL = 0;
    public static final int RSA_INTEGRATED = 1;

    // Constants for indication of RMLSA algorithms
    // traffic grooming
    private static final String GROOMING_NOTRAFFICGROOMING = "notrafficgrooming";
    private static final String GROOMING_SIMPLETRAFFICGROOMING = "simpletrafficgrooming";
    private static final String GROOMING_MGFCCF = "mgfccf";//equivalent to mtgsr
    private static final String GROOMING_MTGSR = "mtgsr";
    private static final String GROOMING_MGFCCFSRNP = "mgfccfsrnp";//equivalent to mtgsr_srnp
    private static final String GROOMING_MTGSRSRNP = "mtgsr_srnp";
    private static final String GROOMING_AUXILIARYGRAPHGROOMING = "auxiliarygraphgrooming";
    private static final String GROOMING_AUXILIARYGRAPHGROOMINGALT = "agg";
    private static final String GROOMING_AUXILIARYGRAPHGROOMINGSRNP = "auxiliarygraphgrooming_srnp";
    private static final String GROOMING_AUXILIARYGRAPHGROOMINGSRNPALT = "agg_srnp";

    // Routing
    private static final String ROUTING_DJK = "djk";
    private static final String ROUTING_MMRDS = "mmrds";
    private static final String ROUTING_FIXEDROUTES = "fixedroutes";

    // Spectrum assignment
    private static final String SPECTRUM_ASSIGNMENT_FISTFIT = "firstfit";
    private static final String SPECTRUM_ASSIGNMENT_BESTFIT = "bestfit";
    private static final String SPECTRUM_ASSIGNMENT_WORSTFIT = "worstfit";
    private static final String SPECTRUM_ASSIGNMENT_EXACTFIT = "exactfit";
    private static final String SPECTRUM_ASSIGNMENT_LASTFIT = "lastfit";
    private static final String SPECTRUM_ASSIGNMENT_RANDOMFIT = "randomfit";
    private static final String SPECTRUM_ASSIGNMENT_FIRSTLASTFIT = "firstlastfit";
    private static final String SPECTRUM_ASSIGNMENT_FIRSTLASTEXACTFIT = "firstlastexactfit";
    private static final String SPECTRUM_ASSIGNMENT_TBSA = "tbsa";
    private static final String SPECTRUM_ASSIGNMENT_DAFLF = "daflf";
    private static final String SPECTRUM_ASSIGNMENT_SAIR = "sair";
    
    // Integrados
    private static final String INTEGRATED_COMPLETESHARING = "completesharing";
    private static final String INTEGRATED_PSEUDOPARTITION = "pseudopartition";
    private static final String INTEGRATED_DEDICATEDPARTITION = "dedicatedpartition";
    private static final String INTEGRATED_LOADBALANCEDDEDICATEDPARTITION = "loadbalanceddedicatedpartition";
    private static final String INTEGRATED_ZONEPARTITION = "zonepartition";
    private static final String INTEGRATED_ZONEPARTITIONTOPINVASION = "zonepartitiontopinvasion";
    private static final String INTEGRATED_KSPFIRSTFIT = "kspfirstfit";
    private static final String INTEGRATED_KSPFIRSTFITSSTG = "kspfirstfit_sstg";
    private static final String INTEGRATED_COMPLETESHARINGEX = "completesharingex";
    private static final String INTEGRATED_COMPLETESHARINGESPAT = "completesharing_espat";
    private static final String INTEGRATED_COMPLETESHARINGEX2 = "completesharingex2";
    private static final String INTEGRATED_COMPLETESHARINGSSTG = "completesharing_sstg";
    private static final String INTEGRATED_KSPSA = "kspsa";
    private static final String INTEGRATED_KSPSA_v2 = "kspsav2";
    private static final String INTEGRATED_KSPC = "kspc";
    private static final String INTEGRATED_MDPC= "mdpc";
    private static final String INTEGRATED_KSPRQOTO = "ksprqoto";
    
    // Regenerator assignment
    private static final String ALL_ASSIGNMENT_OF_REGENERATOR = "aar";
    private static final String FLR_REGENERATOR_ASSIGNMENT = "flrra";
	private static final String FNS_REGENERATOR_ASSIGNMENT = "fnsra";
	
	// Modulation selection
	private static final String MODULATION_BY_DISTANCE = "modulationbydistance";
	private static final String MODULATION_BY_DISTANCE2 = "modulationbydistance2";
	private static final String MODULATION_BY_QOT = "modulationbyqot";
	private static final String MODULATION_BY_QOT_SIGMA = "modulationbyqotsigma";
	private static final String MODULATION_BY_QOT_V2 = "modulationbyqotv2";
	private static final String MODULATION_BY_DISTANCE_BANDWIDTH = "modulationbydistancebandwidth";
	
    // End of constants

    private String grooming;
    private String integrated;
    private String routing;
    private String modulationSelection;
    private String spectrumAssignmentType;
    private String regeneratorAssignment;

    /**
     * Creates a new instance of GRMLSA
     * 
     * @param grooming String
     * @param integrated String
     * @param routing String
     * @param modulationSelection String
     * @param spectrumAssignmentType String
     */
    public GRMLSA(String grooming, String integrated, String routing, String modulationSelection, String spectrumAssignmentType, String regeneratorAssignment) {
        this.grooming = grooming;
        this.integrated = integrated;
        this.routing = routing;
        this.modulationSelection = modulationSelection;
        this.spectrumAssignmentType = spectrumAssignmentType;
        this.regeneratorAssignment = regeneratorAssignment;

        if(grooming == null) this.grooming ="";
        if(integrated == null) this.integrated ="";
        if(routing == null) this.routing ="";
        if(modulationSelection == null) this.modulationSelection ="";
        if(spectrumAssignmentType == null) this.spectrumAssignmentType ="";
        if(regeneratorAssignment == null) this.regeneratorAssignment = "";
    }

    /**
     * Instance the optical traffic aggregation algorithm
     * 
     * @throws Exception
     * @return TrafficGroomingAlgorithm
     */
    public TrafficGroomingAlgorithmInterface instantiateGrooming(){
        switch (this.grooming) {
            case "":
            case GROOMING_NOTRAFFICGROOMING:
                return new NoTrafficGrooming();
            case GROOMING_SIMPLETRAFFICGROOMING:
                return new SimpleTrafficGrooming();
            case GROOMING_MGFCCF:
            case GROOMING_MTGSR: //equivalent
                return new MTGSR();
            case GROOMING_MGFCCFSRNP:
            case GROOMING_MTGSRSRNP: //equivalent
                return new MTGSR_SRNP();
            case GROOMING_AUXILIARYGRAPHGROOMING:
            case GROOMING_AUXILIARYGRAPHGROOMINGALT:
                return new AuxiliaryGraphGrooming();
            case GROOMING_AUXILIARYGRAPHGROOMINGSRNP:
            case GROOMING_AUXILIARYGRAPHGROOMINGSRNPALT:
                return new AuxiliaryGraphGrooming_SRNP();
            default:
                return null;
        }
    }

    /**
     * Instance the routing algorithm
     *
     * @throws Exception
     * @return RoutingInterface
     */
    public RoutingAlgorithmInterface instantiateRouting(){
        switch (this.routing) {
            case ROUTING_DJK:
                return new DJK();
            case ROUTING_FIXEDROUTES:
                return new FixedRoutes();
            case ROUTING_MMRDS:
                return new MMRDS();
            default:
                return null;
        }
    }

    /**
     * Instance the spectrum assignment algorithm
     *
     * @throws Exception
     * @return SpectrumAssignmentInterface
     */
    public SpectrumAssignmentAlgorithmInterface instantiateSpectrumAssignment(){
        switch (this.spectrumAssignmentType) {
            case SPECTRUM_ASSIGNMENT_FISTFIT:
                return new FirstFit();
            case SPECTRUM_ASSIGNMENT_BESTFIT:
                return new BestFit();
            case SPECTRUM_ASSIGNMENT_WORSTFIT:
                return new WorstFit();
            case SPECTRUM_ASSIGNMENT_EXACTFIT:
                return new ExactFit();
            case SPECTRUM_ASSIGNMENT_LASTFIT:
                return new LastFit();
            case SPECTRUM_ASSIGNMENT_RANDOMFIT:
                return new RandomFit();
            case SPECTRUM_ASSIGNMENT_FIRSTLASTFIT:
                return new FirstLastFit();
            case SPECTRUM_ASSIGNMENT_FIRSTLASTEXACTFIT:
            	return new FirstLastExactFit();
            case SPECTRUM_ASSIGNMENT_TBSA:
                return new TrafficBalancingSpectrumAssignment();
            case SPECTRUM_ASSIGNMENT_DAFLF:
                return new DispersionAdaptiveFirstLastFit();
            case SPECTRUM_ASSIGNMENT_SAIR:
                return new SpectrumAssignmentWithInterferenceReduction();
            default:
                return null;
        }
    }

    /**
     * Instance the integrated RMLSA algorithm
     *
     * @throws Exception
     * @return IntegratedRSAAlgoritm
     */
    public IntegratedRMLSAAlgorithmInterface instantiateIntegratedRSA(){
        switch (this.integrated) {
            case INTEGRATED_COMPLETESHARING:
                return new CompleteSharing();
            case INTEGRATED_PSEUDOPARTITION:
                return new PseudoPartition();
            case INTEGRATED_DEDICATEDPARTITION:
                return new DedicatedPartition();
            case INTEGRATED_LOADBALANCEDDEDICATEDPARTITION:
                return new LoadBalancedDedicatedPartition();
            case INTEGRATED_ZONEPARTITION:
                return new ZonePartition();
            case INTEGRATED_ZONEPARTITIONTOPINVASION:
                return new ZonePartitionTopInvasion();
            case INTEGRATED_KSPFIRSTFIT:
                return new KSPFirstFit();
            case INTEGRATED_COMPLETESHARINGEX:
            case INTEGRATED_COMPLETESHARINGESPAT:
                return new CompleteSharingEsPAT();
            case INTEGRATED_COMPLETESHARINGEX2:
            case INTEGRATED_COMPLETESHARINGSSTG:
                return new CompleteSharingSSTG();
            case INTEGRATED_KSPSA:
                return new KShortestPathsAndSpectrumAssignment();
            case INTEGRATED_KSPSA_v2:
                return new KShortestPathsAndSpectrumAssignment_v2();
            case INTEGRATED_KSPC:
                return new KShortestPathsComputation();
            case INTEGRATED_MDPC:
                return new ModifiedDijkstraPathsComputation();
            case INTEGRATED_KSPRQOTO:
                return new KShortestPathsReductionQoTO();
            case INTEGRATED_KSPFIRSTFITSSTG:
                return new KSPFirstFitSSTG();
            default:
                return null;
        }
    }
    
    /**
     * Instance the regenerators assignment algorithm
     * 
     * @throws Exception
     * @return RegeneratorAssignmentAlgorithmInterface
     */
    public RegeneratorAssignmentAlgorithmInterface instantiateRegeneratorAssignment(){
    	switch (this.regeneratorAssignment) {
    		case ALL_ASSIGNMENT_OF_REGENERATOR:
    			return new AllAssignmentOfRegenerator();
    		case FLR_REGENERATOR_ASSIGNMENT:
				return new FLRRegeneratorAssignment();
			case FNS_REGENERATOR_ASSIGNMENT:
				return new FNSRegeneratorAssignment();
    		default:
    			return null;
    	}
    }
    
    /**
     * Instance the modulation selection algorithm
     * 
     * @return ModulationSelectionAlgorithmInterface
     * @throws Exception
     */
    public ModulationSelectionAlgorithmInterface instantiateModulationSelection(){
    	switch (this.modulationSelection) {
	    	case MODULATION_BY_DISTANCE:
	    		return new ModulationSelectionByDistance();
	    	case MODULATION_BY_DISTANCE2:
	    		return new ModulationSelectionByDistance2();
	    	case MODULATION_BY_QOT:
	    		return new ModulationSelectionByQoT();
	    	case MODULATION_BY_QOT_SIGMA:
	    		return new ModulationSelectionByQoTAndSigma();
	    	case MODULATION_BY_QOT_V2:
	    		return new ModulationSelectionByQoTv2();
	    	case MODULATION_BY_DISTANCE_BANDWIDTH:
	    		return new ModulationSelectionByDistanceAndBandwidth();
	    	default:
	    		return null;
    	}
    }
}

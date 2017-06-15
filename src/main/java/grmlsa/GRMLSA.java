package grmlsa;

import grmlsa.integrated.CompleteSharing;
import grmlsa.integrated.DedicatedPartition;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.integrated.LoadBalancedDedicatedPartition;
import grmlsa.integrated.PseudoPartition;
import grmlsa.integrated.ZonePartition;
import grmlsa.integrated.ZonePartitionTopInvasion;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.modulation.ModulationSelectionByDistance;
import grmlsa.modulation.ModulationSelectionByDistance2;
import grmlsa.modulation.ModulationSelectionByQoT;
import grmlsa.regeneratorAssignment.AllAssignmentOfRegenerator;
import grmlsa.regeneratorAssignment.FLRRegeneratorAssignment;
import grmlsa.regeneratorAssignment.FNSRegeneratorAssignment;
import grmlsa.regeneratorAssignment.RegeneratorAssignmentAlgorithmInterface;
import grmlsa.routing.DJK;
import grmlsa.routing.FixedRoutes;
import grmlsa.routing.RoutingAlgorithmInterface;
import grmlsa.spectrumAssignment.BestFit;
import grmlsa.spectrumAssignment.ExactFit;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import grmlsa.spectrumAssignment.WorstFit;
import grmlsa.trafficGrooming.NoTrafficGrooming;
import grmlsa.trafficGrooming.SimpleTrafficGrooming;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithmInterface;

/**
 * This class should be responsible for running the RSA algorithms, verifying whether the selected 
 * algorithm is of the integrated or sequential type, after activating the algorithm (s) necessary 
 * to allocate the resource to the request
 *
 * @author Iallen
 */
public class GRMLSA {
	
	// Network type
	public static final int TRANSPARENT = 0;
	public static final int TRANSLUCENT = 1;
	
	// Constants that indicate which type are the RSA algorithms (sequential or integrated)
    public static final int RSA_SEQUENCIAL = 0;
    public static final int RSA_INTEGRATED = 1;

    // Constants for indication of RMLSA algorithms
    // Optical traffic aggregation
    public static final String GROOMING_OPT_NOTRAFFICGROOMING = "notrafficgrooming";
    public static final String GROOMING_OPT_SIMPLETRAFFICGROOMING = "simpletrafficgrooming";

    // Routing
    public static final String ROUTING_DJK = "djk";
    public static final String ROUTING_FIXEDROUTES = "fixedroutes";

    // Spectrum assignment
    public static final String SPECTRUM_ASSIGNMENT_FISTFIT = "firstfit";
    public static final String SPECTRUM_ASSIGNMENT_BESTFIT = "bestfit";
    public static final String SPECTRUM_ASSIGNMENT_WORSTFIT = "worstfit";
    public static final String SPECTRUM_ASSIGNMENT_EXACTFIT = "exactfit";
    
    // Integrados
    public static final String INTEGRATED_COMPLETESHARING = "completesharing";
    public static final String INTEGRATED_PSEUDOPARTITION = "pseudopartition";
    public static final String INTEGRATED_DEDICATEDPARTITION = "dedicatedpartition";
    public static final String INTEGRATED_LOADBALANCEDDEDICATEDPARTITION = "loadbalanceddedicatedpartition";
    public static final String INTEGRATED_ZONEPARTITION = "zonepartition";
    public static final String INTEGRATED_ZONEPARTITIONTOPINVASION = "zonepartitiontopinvasion";
    
    // Regenerator assignment
    public static final String ALL_ASSIGNMENT_OF_REGENERATOR = "aar";
    public static final String FLR_REGENERATOR_ASSIGNMENT = "flrra";
	public static final String FNS_REGENERATOR_ASSIGNMENT = "fnsra";
	
	// Modulation selection
	public static final String MODULATION_BY_DISTANCE = "modulationbydistance";
	public static final String MODULATION_BY_DISTANCE2 = "modulationbydistance2";
	public static final String MODULATION_BY_QOT = "modulationbyqot";

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
    public TrafficGroomingAlgorithmInterface instantiateGrooming() throws Exception {
        switch (this.grooming) {
            case GROOMING_OPT_NOTRAFFICGROOMING:
                return new NoTrafficGrooming();
            case GROOMING_OPT_SIMPLETRAFFICGROOMING:
                return new SimpleTrafficGrooming();
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
    public RoutingAlgorithmInterface instantiateRouting() throws Exception {
        switch (this.routing) {
            case ROUTING_DJK:
                return new DJK();
            case ROUTING_FIXEDROUTES:
                return new FixedRoutes();
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
    public SpectrumAssignmentAlgorithmInterface instantiateSpectrumAssignment() throws Exception {

        switch (this.spectrumAssignmentType) {
            case SPECTRUM_ASSIGNMENT_FISTFIT:
                return new FirstFit();
            case SPECTRUM_ASSIGNMENT_BESTFIT:
                return new BestFit();
            case SPECTRUM_ASSIGNMENT_WORSTFIT:
                return new WorstFit();
            case SPECTRUM_ASSIGNMENT_EXACTFIT:
                return new ExactFit();
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
    public IntegratedRMLSAAlgorithmInterface instantiateIntegratedRSA() throws Exception {
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
    public RegeneratorAssignmentAlgorithmInterface instantiateRegeneratorAssignment() throws Exception {
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
    public ModulationSelectionAlgorithmInterface instantiateModulationSelection() throws Exception {
    	switch (this.modulationSelection) {
	    	case MODULATION_BY_DISTANCE:
	    		return new ModulationSelectionByDistance();
	    	case MODULATION_BY_DISTANCE2:
	    		return new ModulationSelectionByDistance2();
	    	case MODULATION_BY_QOT:
	    		return new ModulationSelectionByQoT();
	    	default:
	    		return null;
    	}
    }
}

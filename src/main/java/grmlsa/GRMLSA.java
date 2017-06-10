package grmlsa;

import grmlsa.integrated.*;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelector;
import grmlsa.routing.DJK;
import grmlsa.routing.FixedRoutes;
import grmlsa.routing.RoutingInterface;
import grmlsa.spectrumAssignment.*;
import grmlsa.trafficGrooming.NoTrafficGrooming;
import grmlsa.trafficGrooming.SimpleTrafficGrooming;
import grmlsa.trafficGrooming.TrafficGroomingAlgorithm;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

/**
 * This class should be responsible for running the RSA algorithms, verifying whether the selected 
 * algorithm is of the integrated or sequential type, after activating the algorithm (s) necessary 
 * to allocate the resource to the request
 *
 * @author Iallen
 */
public class GRMLSA {
	
	// Constants that indicate which type are the RSA algorithms (sequential or integrated)
    public static final int RSA_SEQUENCIAL = 0;
    public static final int RSA_INTEGRATED = 1;

    // Constants for indication of RSA algorithms
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

    // End of constants

    private int rsaType;
    private RoutingInterface routing;
    private SpectrumAssignmentAlgoritm spectrumAssignment;
    private IntegratedRSAAlgoritm integrated;
    private ModulationSelector modulationSelector;
    private TrafficGroomingAlgorithm grooming;
    private ControlPlane controlPlane;

    /**
     * Constructor for integrated RSA algorithms
     * 
     * @param integrated String
     * @param slotFrequency double
     * @param controlPlane ControlPlane
     * @throws Exception
     */
    public GRMLSA(String integrated, double slotFrequency, ControlPlane controlPlane) throws Exception {
        this.rsaType = 1;
        instantiateIntegratedRSA(integrated);
        modulationSelector = new ModulationSelector(slotFrequency, controlPlane.getMesh().getGuardBand(), controlPlane);
        instantiateGrooming("notrafficgrooming");
        this.controlPlane = controlPlane;
    }

    /**
     * Constructor for sequential RSA algorithms
     * 
     * @param routingType String
     * @param spectrumAssignmentType String
     * @param spectrumBand double
     * @param controlPlane ControlPlane
     * @throws Exception
     */
    public GRMLSA(String routingType, String spectrumAssignmentType, double spectrumBand, ControlPlane controlPlane) throws Exception {
        this.rsaType = 0;
        instantiateRouting(routingType);
        instantiateSpectrumAssignment(spectrumAssignmentType);
        modulationSelector = new ModulationSelector(spectrumBand, controlPlane.getMesh().getGuardBand(), controlPlane);
        instantiateGrooming("notrafficgrooming");
        this.controlPlane = controlPlane;
    }

    /**
     * Instance the optical traffic aggregation algorithm
     *
     * @param groomingAlgo String
     */
    private void instantiateGrooming(String groomingAlgo) {
        switch (groomingAlgo) {
            case GROOMING_OPT_NOTRAFFICGROOMING:
                this.grooming = new NoTrafficGrooming();
                break;
            case GROOMING_OPT_SIMPLETRAFFICGROOMING:
                this.grooming = new SimpleTrafficGrooming();
                break;
        }
    }

    /**
     * Instance the routing algorithm
     *
     * @param routingType String
     * @throws Exception
     */
    private void instantiateRouting(String routingType) throws Exception {
        switch (routingType) {
            case ROUTING_DJK:
                this.routing = new DJK();
                break;
            case ROUTING_FIXEDROUTES:
                this.routing = new FixedRoutes();
                break;

            default:
                throw new Exception("unknow routing algorithm");
        }
    }

    /**
     * Instance the spectrum assignment algorithm
     *
     * @param spectrumAssignmentType String
     * @throws Exception
     */
    private void instantiateSpectrumAssignment(String spectrumAssignmentType) throws Exception {

        switch (spectrumAssignmentType) {
            case SPECTRUM_ASSIGNMENT_FISTFIT:
                this.spectrumAssignment = new FirstFit();
                break;
            case SPECTRUM_ASSIGNMENT_BESTFIT:
                this.spectrumAssignment = new BestFit();
                break;
            case SPECTRUM_ASSIGNMENT_WORSTFIT:
                this.spectrumAssignment = new WorstFit();
                break;
            case SPECTRUM_ASSIGNMENT_EXACTFIT:
                this.spectrumAssignment = new ExactFit();
                break;

            default:
                throw new Exception("unknow spectrum assignment algorithm");
        }
    }

    /**
     * Instance the integrated RSA algorithm
     *
     * @param integratedRSAType String
     * @throws Exception
     */
    private void instantiateIntegratedRSA(String integratedRSAType) throws Exception {
        switch (integratedRSAType) {
            case INTEGRATED_COMPLETESHARING:
                this.integrated = new CompleteSharing();
                break;
            case INTEGRATED_PSEUDOPARTITION:
                this.integrated = new PseudoPartition();
                break;
            case INTEGRATED_DEDICATEDPARTITION:
                this.integrated = new DedicatedPartition();
                break;
            case INTEGRATED_LOADBALANCEDDEDICATEDPARTITION:
                this.integrated = new LoadBalancedDedicatedPartition();
                break;
            case INTEGRATED_ZONEPARTITION:
                this.integrated = new ZonePartition();
                break;
            case INTEGRATED_ZONEPARTITIONTOPINVASION:
                this.integrated = new ZonePartitionTopInvasion();
                break;

            default:
                throw new Exception("unknow integrated RSA algorithm");
        }
    }

    /**
     * This method tries to answer a given request by allocating the necessary resources to the same one
     *
     * @param circuit Circuit
     * @return boolean
     */
    public boolean createNewCircuit(Circuit circuit) {

        switch (rsaType) {
            case RSA_INTEGRATED:
                return integrated.rsa(circuit, controlPlane.getMesh());
                
            case RSA_SEQUENCIAL:
                if (routing.findRoute(circuit, this.controlPlane.getMesh())) {
                    Modulation mod = modulationSelector.selectModulation(circuit);
                    circuit.setModulation(mod);
                    return spectrumAssignment.assignSpectrum(mod.requiredSlots(circuit.getRequiredBandwidth()), circuit);
                } else {
                    return false;
                }
        }

        return false;
    }

    /**
     * This method verifies the possibility of satisfying a circuit request
     * 
     * @param rfc RequestForConnection
     * @return boolean
     */
    public boolean handleRequisition(RequestForConnection rfc) {
        return grooming.searchCircuitsForGrooming(rfc, this);
    }

    /**
     * This method ends a connection
     * 
     * @param rfc RequestForConnection
     */
    public void finalizeConnection(RequestForConnection rfc) {
        this.grooming.finishConnection(rfc, this);
    }

    /**
     * This method returns the control plane
     * 
     * @return ControlPlane
     */
    public ControlPlane getControlPlane() {
        return controlPlane;
    }
}

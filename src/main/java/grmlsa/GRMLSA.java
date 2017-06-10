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
 * Esta classe devera ser responsavel por executar os algoritmos de RSA, verificando se o algoritmo selecionado eh do tipo
 * integrado ou sequencial, apos acionar o/os algoritmo/s necessarios para alocar o recurso para a requisicao
 *
 * @author Iallen
 */
public class GRMLSA {
    //Constantes que indicam que tipo são os algoritmos RSA (sequenciais ou integrados)
    public static final int RSA_SEQUENCIAL = 0;
    public static final int RSA_INTEGRATED = 1;

    //Constantes para indicação dos algoritmos RSA
    //Agregação óptica de tráfego
    public static final String GROOMING_OPT_NOTRAFFICGROOMING = "notrafficgrooming";
    public static final String GROOMING_OPT_SIMPLETRAFFICGROOMING = "simpletrafficgrooming";

    //Roteamento
    public static final String ROUTING_DJK = "djk";
    public static final String ROUTING_FIXEDROUTES = "fixedroutes";

    //Alocação de espectro
    public static final String SPECTRUM_ASSIGNMENT_FISTFIT = "firstfit";
    public static final String SPECTRUM_ASSIGNMENT_BESTFIT = "bestfit";
    public static final String SPECTRUM_ASSIGNMENT_WORSTFIT = "worstfit";
    public static final String SPECTRUM_ASSIGNMENT_EXACTFIT = "exactfit";

    //Integrados
    public static final String INTEGRATED_COMPLETESHARING = "completesharing";
    public static final String INTEGRATED_PSEUDOPARTITION = "pseudopartition";
    public static final String INTEGRATED_DEDICATEDPARTITION = "dedicatedpartition";
    public static final String INTEGRATED_LOADBALANCEDDEDICATEDPARTITION = "loadbalanceddedicatedpartition";
    public static final String INTEGRATED_ZONEPARTITION = "zonepartition";
    public static final String INTEGRATED_ZONEPARTITIONTOPINVASION = "zonepartitiontopinvasion";

    //fim das constantes

    private int rsaType;
    private RoutingInterface routing;
    private SpectrumAssignmentAlgoritm spectrumAssignment;
    private IntegratedRSAAlgoritm integrated;
    private ModulationSelector modulationSelector;
    private TrafficGroomingAlgorithm grooming;
    private ControlPlane controlPlane;

    public GRMLSA(String integrated, double slotFrequency, ControlPlane controlPlane) throws Exception {
        this.rsaType = 1;
        instantiateIntegratedRSA(integrated);
        modulationSelector = new ModulationSelector(slotFrequency, controlPlane.getMesh().getGuardBand());
        instantiateGrooming("notrafficgrooming");
        this.controlPlane = controlPlane;
    }

    public GRMLSA(String routingType, String spectrumAssignmentType, double spectrumBand, ControlPlane controlPlane) throws Exception {
        this.rsaType = 0;
        instantiateRouting(routingType);
        instantiateSpectrumAssignment(spectrumAssignmentType);
        modulationSelector = new ModulationSelector(spectrumBand, controlPlane.getMesh().getGuardBand());
        instantiateGrooming("notrafficgrooming");
        this.controlPlane = controlPlane;
    }

    /**
     * instancia o algoritmo de agregação óptica de tráfego
     *
     * @param groomingAlgo
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
     * instancia o algoritmo de roteamento
     *
     * @param routingType
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
     * instancia o algoritmo de alocação de espectro
     *
     * @param spectrumAssignmentType
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
     * instancia o algoritmo de RSA integrado
     *
     * @param integratedRSAType
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
     * Este método tenta atender uma determinada requisição alocando os devidos recursos para a mesma
     *
     * @param request
     * @return
     */
    public boolean criarNovoCircuito(Circuit request) {

        switch (rsaType) {
            case RSA_INTEGRATED:
                return integrated.rsa(request, controlPlane.getMesh());
            case RSA_SEQUENCIAL:
                if (routing.findRoute(request, this.controlPlane.getMesh())) {
                    Modulation mod = modulationSelector.selectModulation(request);
                    request.setModulation(mod);
                    return spectrumAssignment.assignSpectrum(mod.requiredSlots(request.getRequiredBandwidth()), request);
                } else {
                    return false;
                }
        }

        return false;
    }

    public boolean atenderRequisicao(RequestForConnection rfc) {
        return grooming.searchCircuitsForGrooming(rfc, this);
    }

    public void finalizarConexao(RequestForConnection rfc) {
        this.grooming.finishConnection(rfc, this);
    }

    public ControlPlane getControlPlane() {
        return controlPlane;
    }
}

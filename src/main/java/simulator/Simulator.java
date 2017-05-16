package simulator;

import measurement.Measurements;
import network.Pair;
import network.RequestGenerator;
import simulator.eventListeners.ArriveRequestForConexionListener;

public class Simulator {

    private EventMachine eMachine;
    private ArriveRequestForConexionListener arriveRequest;
    private Simulation simulation;

    /**
     * Constroi um obj Simulator.
     *
     * @param simulation Simulation
     */
    public Simulator(Simulation simulation) {
        this.simulation = simulation;
    }

//------------------------------------------------------------------------------

    /**
     * Inicia a Simulação. Para isso é necessário: (1) carregar a malha; (2)criar
     * as instâncias de arriveRequest e FinalizeRequest; (3) agendar os primeiro
     * eventos de chegada de requisição.
     *
     * @return
     */
    public Measurements start() {
        eMachine = new EventMachine();
        // criando o escutador de eventos arriveRequest
        arriveRequest = new ArriveRequestForConexionListener(this.getEventMachine(), simulation);
        this.scheduleFirstEvents();
        this.eMachine.executeEvents();
        return this.simulation.getMeasurements();
    }


    //------------------------------------------------------------------------------

    /**
     * Agenda o primeiro eventos de chegada de requisicao. Isto é feito para cada gerador de requisições da rede
     */
    private void scheduleFirstEvents() {

        for (Pair pair : simulation.getMesh().getPairList()) {
            for (RequestGenerator rg : pair.getRequestGenerators()) {
                rg.scheduleNextRequest(eMachine, arriveRequest);
            }
        }
    }


    //------------------------------------------------------------------------------
    public EventMachine getEventMachine() {
        return this.eMachine;
    }

    //------------------------------------------------------------------------------
    public Simulation getSimulation() {
        return this.simulation;
    }
}

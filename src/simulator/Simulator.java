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
     * Inicia a Simula��o. Para isso � necess�rio: (1) carregar a malha; (2)criar
     * as inst�ncias de arriveRequest e FinalizeRequest; (3) agendar os primeiro
     * eventos de chegada de requisi��o.
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
     * Agenda o primeiro eventos de chegada de requisicao. Isto � feito para cada gerador de requisi��es da rede
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

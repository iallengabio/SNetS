package simulator.eventListeners;

import measurement.Measurements;
import request.RequestForConnection;
import simulator.Event;
import simulator.EventMachine;
import simulator.Simulation;

import java.io.Serializable;

/**
 * Esta classe � o escutador do evento de uma chegada de uma requisi��o por uma nova conex�o.
 * Esta classe trata cada requisi��o por uma nova conex�o tentando alocar um novo circuito �ptico.
 *
 * @author Iallen
 */
public class ArriveRequestForConexionListener implements EventListener, Serializable {

    private EventMachine em;
    private Simulation simulation;

    public ArriveRequestForConexionListener(EventMachine em, Simulation simulation) {
        this.em = em;
        this.simulation = simulation;
    }

    @Override
    public void execute(Event e) {

        RequestForConnection requestForConnection = (RequestForConnection) e.getObject();

        //agendar pr�xima requisi��o para conex�o com este mesmo requestGenerator
        Measurements m = simulation.getMeasurements();
        if (!m.finished()) { //agendar outra requisicao atraves do mesmo gerador desta
            requestForConnection.getRequestGenerator().scheduleNextRequest(em, this);
        }


        beforeReq();
        //tentar atender a requisi��o
        Boolean success = simulation.getControlPlane().atenderRequisicao(requestForConnection);
        if (success) {//agendar o fim da requisi��o e libera��o dos recursos
            em.insert(new Event(requestForConnection, new HoldRequestListener(simulation), requestForConnection.getTimeOfFinalizeHours()));
        }
        afterReq(requestForConnection, success);


    }

    private void beforeReq() {
        Measurements m = simulation.getMeasurements();

        m.transientStepVerify(simulation.getControlPlane().getMesh().getNodeList());

        m.incNumGeneratedReq();
    }

    private void afterReq(RequestForConnection request, boolean success) {

        Measurements m = simulation.getMeasurements();
        m.getProbabilidadeDeBloqueioMeasurement().addNewObservation(success, request);
        m.getProbabilidadeDeBloqueioDeBandaMeasurement().addNewObservation(success, request);
        m.getFragmentacaoExterna().addNewObservation(request.getCircuit());
        m.getUtilizacaoSpectro().addNewObservation();
        m.getFragmentacaoRelativa().addNewObservation(request.getCircuit());
        m.getSpectrumSizeStatistics().addNewObservation(request.getCircuit());
        m.getTransmitersReceiversUtilization().addNewObservation();

    }


}

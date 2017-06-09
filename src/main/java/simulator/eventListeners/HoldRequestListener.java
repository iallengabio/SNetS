package simulator.eventListeners;

import request.RequestForConnection;
import simulator.Event;
import simulator.Simulation;

import java.io.Serializable;


public class HoldRequestListener implements EventListener, Serializable {
    private Simulation simulation;

    public HoldRequestListener(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public void execute(Event e) {
        RequestForConnection request = (RequestForConnection) e.getObject();
        simulation.getControlPlane().finalizarConexao(request);
    }

}

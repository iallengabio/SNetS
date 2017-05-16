package simulator.eventListeners;

import request.RequestForConexion;
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
        RequestForConexion request = (RequestForConexion) e.getObject();
        simulation.getControlPlane().finalizarConexao(request);
    }

}

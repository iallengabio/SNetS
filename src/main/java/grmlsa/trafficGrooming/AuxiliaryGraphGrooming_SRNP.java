package grmlsa.trafficGrooming;

import grmlsa.trafficGrooming.util.SRNP;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

public class AuxiliaryGraphGrooming_SRNP extends AuxiliaryGraphGrooming {
    private SRNP srnp;

    @Override
    protected void init(ControlPlane cp) {
        srnp = new SRNP(cp);
        super.init(cp);
    }

    @Override
    protected boolean establishCircuit(Circuit c, ControlPlane cp) {
        try {
            return srnp.establishCircuit(c);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {
        for (Circuit circuit : rfc.getCircuits()) {
            if (circuit.getRequests().size() == 1) {
                cp.releaseCircuit(circuit);
            } else {
                srnp.retractCircuit(circuit,rfc);
            }
        }
    }
}

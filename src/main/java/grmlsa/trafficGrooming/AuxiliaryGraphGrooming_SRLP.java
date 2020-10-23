package grmlsa.trafficGrooming;

import grmlsa.trafficGrooming.util.SRLP;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

public class AuxiliaryGraphGrooming_SRLP extends AuxiliaryGraphGrooming {
    private SRLP srlp;

    @Override
    protected void init(ControlPlane cp) {
        srlp = new SRLP(cp);
        super.init(cp);
    }

    @Override
    protected boolean establishCircuit(Circuit c, ControlPlane cp) {
        try {
            return srlp.establishCircuit(c);
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
                srlp.retractCircuit(circuit,rfc);
            }
        }
    }
}

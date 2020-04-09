package grmlsa.trafficGrooming;

import grmlsa.trafficGrooming.util.SRNP;
import network.Circuit;
import network.ControlPlane;
import network.Node;
import network.Pair;
import request.RequestForConnection;

import java.util.ArrayList;

/**
 * This class represents a Multihop Traffic Grooming algorithm whith the spectrum reservation scheme presented in "Dynamic Traf?c Grooming in Sliceable Bandwidth-Variable Transponder-Enabled Elastic Optical Networks".
 *
 * Extends this class to implement diferent trafic grooming policies.
 */
public abstract class MultihopGroomingSRNP extends MultihopGrooming {

    private SRNP srnp;

    protected Circuit complementSolution(MultihopGroomingSRNP.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Node s = ms.pairComplement.getSource();
        Node d = ms.pairComplement.getDestination();
        Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));
        if (!srnp.establishCircuit(newCircuit)) {
            return null;
        } else {
            newCircuit.removeRequest(rfc);
            rfc.setCircuit(new ArrayList<>());
            return newCircuit;
        }
    }

    public boolean searchCircuitsForGrooming(final RequestForConnection rfc, final ControlPlane cp) throws Exception {
        if(srnp==null){
            srnp = new SRNP(cp);
        }
        return super.searchCircuitsForGrooming(rfc,cp);
    }

    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {

        for (Circuit circuit : rfc.getCircuits()) {
            if (circuit.getRequests().size() == 1) {
                cp.releaseCircuit(circuit);
                this.removeCircuitVirtualRouting(circuit);
            } else {
                srnp.retractCircuit(circuit,rfc);
            }
        }

    }

}




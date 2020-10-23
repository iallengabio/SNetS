package grmlsa.trafficGrooming.util;

import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

/**
 * Implements the Algorithm SRLP described in "Dynamic Traffic Grooming in Sliceable Bandwidth-Variable Transponder-Enabled Elastic Optical Networks".
 */
public class SRLP {

    private final ControlPlane cp;
    private final double reservationTarget;

    public SRLP(ControlPlane cp){
        this.cp = cp;
        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
        this.reservationTarget = Double.parseDouble((String)uv.get("reservationTarget"));
    }

    public boolean establishCircuit(Circuit circuit) throws Exception{
        RequestForConnection tempReq = new RequestForConnection();
        tempReq.setPair(circuit.getPair());

        tempReq.setRequiredBandwidth(reservationTarget);
        circuit.addRequest(tempReq);

        if(cp.establishCircuit(circuit)){//try stabilish with reserve
            circuit.removeRequest(tempReq);
            //reservesByNode.put(pair,reservationTarget);
            return true;
        }

        circuit.removeRequest(tempReq); //try stabilish without reserve
        return cp.establishCircuit(circuit);//fail to stabilish new circuit
    }

    public double getReservationTarget() {
        return reservationTarget;
    }


    public void retractCircuit(Circuit circuit, RequestForConnection rfc) throws Exception {
        circuit.removeRequest(rfc);
        //Double actualReserve = this.getReservesByNode().get(circuit.getPair().getName());
        double actualReserve = circuit.getResidualCapacity();
        //Double retract = actualReserve - this.getReservationTarget();
        double retractInThisCirc = actualReserve - this.getReservationTarget();

        if (retractInThisCirc > 0) {//retract the circuit

            int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getBandwidth() - retractInThisCirc);
            int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
            int release = numCurrentSlots - numFinalSlots;
            //int[] releaseBand = new int[2];
            if (release != 0) {
                //releaseBand[1] = circuit.getSpectrumAssigned()[1];
                //releaseBand[0] = releaseBand[1] - release + 1;
                cp.retractCircuit(circuit, 0, release);
            }

        }

    }


}

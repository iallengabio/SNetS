package grmlsa.trafficGrooming.util;

import network.Circuit;
import network.ControlPlane;
import network.Pair;
import request.RequestForConnection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements the Spectrum Reservation by Node Pair presented in "Dynamic Traf?c Grooming in Sliceable Bandwidth-Variable Transponder-Enabled Elastic Optical Networks".
 */
public class SRNP {

    private ControlPlane cp;
    private double reservationTarget = 0;
    private HashMap<String,Double> reservesByNode = new HashMap<>();

    public SRNP(ControlPlane cp){
        this.cp = cp;
        Iterator<Pair> iterator = cp.getMesh().getPairList().iterator();
        while(iterator.hasNext()){
            reservesByNode.put(iterator.next().getName(),0.0);
        }
        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
        this.reservationTarget = Double.parseDouble((String)uv.get("reservationTarget"));
    }

    public void computeResidualCapacity(){
        Iterator<Pair> iterator = cp.getMesh().getPairList().iterator();
        while(iterator.hasNext()){
            reservesByNode.put(iterator.next().getName(),0.0);
        }
        Iterator<Circuit> iterator2 = cp.getConnections().iterator();
        while(iterator2.hasNext()){
            Circuit c = iterator2.next();
            reservesByNode.put(c.getPair().getName(),reservesByNode.get(c.getPair().getName())+c.getResidualCapacity());

        }
    }

    public boolean establishCircuit(Circuit circuit) throws Exception{
        computeResidualCapacity();
        RequestForConnection tempReq = new RequestForConnection();
        tempReq.setPair(circuit.getPair());
        String pair = circuit.getPair().getPairName();
        double newReserve = reservationTarget - reservesByNode.get(pair);

        if(newReserve>0) {
            //System.out.println("está reservando - " + reservationTarget + "  -  " + reservesByNode.get(pair));
            tempReq.setRequiredBandwidth(newReserve);
            circuit.addRequest(tempReq);
        }
        if(cp.establishCircuit(circuit)){//try stabilish with reserve
            circuit.removeRequest(tempReq);
            //reservesByNode.put(pair,reservationTarget);
            return true;
        }

        circuit.removeRequest(tempReq); //try stabilish without reserve
        if(cp.establishCircuit(circuit)){
            return true;
        }

        return false; //fail to stabilish new circuit
    }

    public double getReservationTarget() {
        return reservationTarget;
    }

    public HashMap<String, Double> getReservesByNode() {
        return reservesByNode;
    }

    public void retractCircuit(Circuit circuit, RequestForConnection rfc) throws Exception {
        circuit.removeRequest(rfc);
        this.computeResidualCapacity();
        Double actualReserve = this.getReservesByNode().get(circuit.getPair().getName());
        Double retract = actualReserve - this.getReservationTarget();
        Double retractInThisCirc;
        if (circuit.getResidualCapacity() > retract) {
            retractInThisCirc = retract;
        } else {
            retractInThisCirc = circuit.getResidualCapacity();
        }
        if (retractInThisCirc > 0) {//retract the circuit

            int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getBandwidth() - retractInThisCirc);
            int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
            int release = numCurrentSlots - numFinalSlots;
            int[] releaseBand = new int[2];
            if (release != 0) {
                releaseBand[1] = circuit.getSpectrumAssigned()[1];
                releaseBand[0] = releaseBand[1] - release + 1;
                cp.retractCircuit(circuit, 0, release);
            }

        }

    }


}

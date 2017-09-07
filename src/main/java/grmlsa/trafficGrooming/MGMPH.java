package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import network.Node;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

/**
 * This class represents a Multihop Grooming with Min Physical Hops policy.
 *
 *
 *
 * <p>
 * Created by Iallen on 10/08/2017.
 */
public class MGMPH implements TrafficGroomingAlgorithmInterface {

    private int maxVirtualHops = 3;

    private HashMap<String, HashMap<String, ArrayList<ArrayList<Circuit>>>> virtualRouting;


    @Override
    public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) throws Exception {
        if (virtualRouting == null) {
            initVirtualRouting(cp);
        }

        if (simpleEletricGrooming(rfc)) {
            return true;
        }

        if (expandEletricGrooming(rfc, cp)) {
            return true;
        }

        // Try to create a new circuit to accommodate that.
        Circuit circuit = cp.createNewCircuit(rfc);

        if (cp.establishCircuit(circuit)) {
            addNewCircuitVirtualRouting(circuit);
            return true;
        }

        return false;
    }

    @Override
    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {

        for (Circuit circuit : rfc.getCircuits()) {

            if (circuit.getRequests().size() == 1) { // The connection being terminated is the last to use this channel.
                cp.releaseCircuit(circuit);
                removeCircuitVirtualRouting(circuit);
            } else { // Reduce the number of slots allocated for this channel if it is possible.

                int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() - rfc.getRequiredBandwidth());
                int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
                int release = numCurrentSlots - numFinalSlots;
                int releaseBand[] = new int[2];

                if (release != 0) {
                    releaseBand[1] = circuit.getSpectrumAssigned()[1];
                    releaseBand[0] = releaseBand[1] - release + 1;
                    cp.retractCircuit(circuit, null, releaseBand);

                }

                circuit.removeRequest(rfc);
            }
        }

    }

    /**
     * Initializes the structure that stores the virtual routing solutions.
     * @param cp
     */
    private void initVirtualRouting(ControlPlane cp) {
        virtualRouting = new HashMap<>();
        Iterator<Node> it1 = cp.getMesh().getNodeList().iterator();
        while (it1.hasNext()) {
            String src = it1.next().getName();
            virtualRouting.put(src, new HashMap<>());
            Iterator<Node> it2 = cp.getMesh().getNodeList().iterator();
            while (it2.hasNext()) {
                String dst = it2.next().getName();
                virtualRouting.get(src).put(dst, new ArrayList<>());
            }
        }
    }

    /**
     * Updates the structure that stores the virtual routing solutions when a new physical circuit is established.
     * @param circuit
     */
    private void addNewCircuitVirtualRouting(Circuit circuit) {
        //update virtual Routing
        String dst = circuit.getPair().getDestination().getName();
        String src = circuit.getPair().getSource().getName();
        ArrayList<Circuit> al = new ArrayList<>();
        al.add(circuit);
        virtualRouting.get(src).get(dst).add(al);

        ArrayList<ArrayList<Circuit>> aalAux = new ArrayList<>();
        aalAux.add(al);

        //update virtual routes with starts in the new circuit
        Iterator<ArrayList<ArrayList<Circuit>>> it = virtualRouting.get(dst).values().iterator();
        while (it.hasNext()) {
            Iterator<ArrayList<Circuit>> it2 = it.next().iterator();
            while (it2.hasNext()) {
                ArrayList<Circuit> clone = (ArrayList<Circuit>) it2.next().clone();
                clone.add(0, circuit);
                if(clone.size()<maxVirtualHops&&!hasVirtualLoop(clone)) {
                    virtualRouting.get(src).get(dst).add(clone);
                    aalAux.add(clone);
                }
            }
        }

        //update virtual routes with ends in src
        for (String sA : virtualRouting.keySet()) {//for each source
            Iterator<ArrayList<Circuit>> it3 = virtualRouting.get(sA).get(src).iterator();
            while (it3.hasNext()) {//each circuit with ends in src
                ArrayList<Circuit> c1 = (ArrayList<Circuit>) it3.next();
                //for all new Solutions (aalAux)
                Iterator<ArrayList<Circuit>> it4 = aalAux.iterator();
                while (it4.hasNext()) {
                    ArrayList<Circuit> cAux = (ArrayList<Circuit>) c1.clone();
                    ArrayList<Circuit> c2 = (ArrayList<Circuit>) it4.next().clone();
                    cAux.addAll(c2);

                    if(cAux.size()<maxVirtualHops&&!hasVirtualLoop(cAux)) {
                        virtualRouting.get(cAux.get(0).getSource().getName()).get(cAux.get(cAux.size() - 1).getDestination().getName()).add(cAux);
                    }
                }
            }
        }
    }

    /**
     * Updates the structure that stores the virtual routing solutions when a new physical circuit is finished.
     * @param circuit
     */
    private void removeCircuitVirtualRouting(Circuit circuit){
        Set<String> srcs = virtualRouting.keySet();
        for(String src : srcs){
            Set<String> dsts = virtualRouting.get(src).keySet();
            for(String dst: dsts){
                ArrayList<ArrayList<Circuit>> virtRoutes = virtualRouting.get(src).get(dst);
                ArrayList<ArrayList<Circuit>> newVR = new ArrayList<>();
                for(ArrayList<Circuit> vr : virtRoutes){
                    if(!vr.contains(circuit)){//maintain only virtual routes that not contains the circuit
                        newVR.add(vr);
                    }
                }
                virtualRouting.get(src).put(dst, newVR);
            }
        }
    }

    /**
     *
     * @param rfc
     * @return
     */
    private boolean simpleEletricGrooming(RequestForConnection rfc) {
        //first try eletrical grooming
        ArrayList<ArrayList<Circuit>> avrsc = allVirtualRoutingWithSuficientResidualCapacity(rfc);
        if (avrsc.size() > 0) {//can do eletrical grooming
            //search the min cost solution
            int i;
            ArrayList<Circuit> chosenCircs = avrsc.get(0);
            for (i = 1; i < avrsc.size(); i++) {
                if (costFunction1(chosenCircs) > costFunction1(avrsc.get(i))) {
                    chosenCircs = avrsc.get(i);
                }
            }

            //do eletrical grooming
            rfc.setCircuit(chosenCircs);
            for (Circuit circuit : chosenCircs) {
                circuit.getRequests().add(rfc);
            }
            return true;
        }
        return false;
    }

    private boolean expandEletricGrooming(RequestForConnection rfc, ControlPlane cp) throws Exception {
        //second try expand some circuits to enable eletrical grooming
        ArrayList<ArrayList<Circuit>> avr = allVirtualRouting(rfc);
        Collections.sort(avr, new Comparator<ArrayList<Circuit>>() {//Order the solutions according to the cost function 2.
            @Override
            public int compare(ArrayList<Circuit> o1, ArrayList<Circuit> o2) {
                Double d1,d2;
                d1 = costFunction2(o1,rfc);
                d2 = costFunction2(o2,rfc);
                return d1.compareTo(d2);
            }
        });
        for (ArrayList<Circuit> alc : avr) {//for each solution
            boolean canBeExpanded = true;
            for (Circuit c : alc) {//all circuits can be expanded?
                int[] exp = Grooming.circuitExpansiveness(c);
                int circExCap = exp[0] + exp[1];
                int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
                canBeExpanded = canBeExpanded && (circExCap > slotsNeeded);
            }
            if (canBeExpanded) {//all circuits can be expanded
                //expand each circuit and acomodate the new request
                List<int[]> upExps = new ArrayList<>();
                List<int[]> downExps = new ArrayList<>();
                int i;
                for (i=0;i<alc.size();i++) {
                    Circuit c = alc.get(i);
                    int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
                    List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute());
                    int[] bandFreeAdjInferior = IntersectionFreeSpectrum.bandAdjacentInferior(c.getSpectrumAssigned(), composition);
                    int[] bandFreeAdjSuperior = IntersectionFreeSpectrum.bandAdjacentSuperior(c.getSpectrumAssigned(), composition);
                    int expansion[] = decideToExpand(slotsNeeded, bandFreeAdjInferior, bandFreeAdjSuperior);
                    int upExpBand[] = upExpBand(c,rfc,expansion[1]);
                    int downExpBand[] = downExpBand(c,rfc,expansion[0]);
                    upExps.add(upExpBand);
                    downExps.add(downExpBand);

                    if (cp.expandCircuit(c, upExpBand, downExpBand)) {// Expansion succeeded
                        c.addRequest(rfc);
                        rfc.getCircuits().add(c);
                    }else{
                        canBeExpanded = false; //Undo the expansion of circuits
                        break;
                    }
                }
                if(canBeExpanded) {
                    return true;
                }else {
                    i--;
                    for (; i >= 0; i--) {//Undo the expansion of circuits
                        cp.retractCircuit(alc.get(i), downExps.get(i),upExps.get(i));
                        alc.get(i).removeRequest(rfc);
                        rfc.getCircuits().remove(alc.get(i));
                    }
                }
            }
        }
        return false;
    }

    private ArrayList<ArrayList<Circuit>> allVirtualRoutingWithSuficientResidualCapacity(RequestForConnection rfc) {
        ArrayList<ArrayList<Circuit>> res = new ArrayList<>();
        for (ArrayList<Circuit> sol : virtualRouting.get(rfc.getPair().getSource().getName()).get(rfc.getPair().getDestination().getName())) {
            boolean haveSuficientResitualCapacity = true;
            for (Circuit circuit : sol) {
                haveSuficientResitualCapacity = haveSuficientResitualCapacity && (circuit.getResidualCapacity() > rfc.getRequiredBandwidth());
            }
            if (haveSuficientResitualCapacity) res.add(sol);
        }
        return res;
    }

    private ArrayList<ArrayList<Circuit>> allVirtualRouting(RequestForConnection rfc) {
        return virtualRouting.get(rfc.getPair().getSource().getName()).get(rfc.getPair().getDestination().getName());
    }

    private static boolean hasVirtualLoop(ArrayList<Circuit> sol){
        HashSet<String> nAux = new HashSet<>();
        nAux.add(sol.get(0).getPair().getSource().getName());

        for (Circuit c : sol){
            if(nAux.contains(c.getPair().getDestination().getName())) return true;
            nAux.add(c.getPair().getDestination().getName());
        }

        return false;
    }

    /**
     * This cost function is used to compare simple eletric grooming solutions.
     * @param sol
     * @return
     */
    private double costFunction1(ArrayList<Circuit> sol) {
        double res = 0;
        for(Circuit circuit : sol){
            res += circuit.getRoute().getHops();
        }
        return res;
    }

    /**
     * This cost function is used to compare solutions of grooming that need to expand some circuits.
     * @param sol
     * @param rfc
     * @return
     */
    private double costFunction2(ArrayList<Circuit> sol, RequestForConnection rfc){
        double res = 0;
        for (Circuit circuit : sol) {
            if (circuit.getResidualCapacity() < rfc.getRequiredBandwidth()) {
                res++;
            }
        }

        res = res * 100 + costFunction1(sol); //In case of a tie, preference should be given to solutions with fewer virtual hops.

        return res;
    }
    /**
     * This method decides how to expand the channel to accommodate new connections.
     *
     * @param numMoreSlots   int - Number of slots that are still needed to establish the circuit
     * @param upperFreeSlots int
     * @param lowerFreeSlots int
     * @return a vector with size 2, the index 0 represents the number of slots to use below (lower),
     * the index 1 represents the number of slots to use above (upper).
     */
    private static int[] decideToExpand(int numMoreSlots, int lowerFreeSlots[], int upperFreeSlots[]) {
        int res[] = new int[2];

        int numLowerFreeSlots = 0;
        if (lowerFreeSlots != null) {
            numLowerFreeSlots = lowerFreeSlots[1] - lowerFreeSlots[0] + 1;
        }

        int numUpperFreeSlots = 0;
        if (upperFreeSlots != null) {
            numUpperFreeSlots = upperFreeSlots[1] - upperFreeSlots[0] + 1;
        }

        if (numLowerFreeSlots >= numMoreSlots) { // First, try to put everything down
            res[0] = numMoreSlots;
            res[1] = 0;
        } else { // Elsewere, use fully down free spectrum band and the remaining on top
            res[0] = numLowerFreeSlots;
            res[1] = numMoreSlots - numLowerFreeSlots;
        }

        return res;
    }

    /**
     * This method calculates the range of slots that will be used for the expansion at the bottom.
     * @param c
     * @param rfc
     * @param expansion
     * @return
     */
    private static int[] downExpBand(Circuit c, RequestForConnection rfc, int expansion){

        List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute());
        int[] bandFreeAdjInferior = IntersectionFreeSpectrum.bandAdjacentInferior(c.getSpectrumAssigned(), composition);

        int downExpBand[] = null;
        if (expansion > 0) { // Expand down
            downExpBand = new int[2];

            downExpBand[1] = bandFreeAdjInferior[1];
            downExpBand[0] = downExpBand[1] - expansion + 1;
        }

        return downExpBand;
    }

    /**
     * This method calculates the range of slots that will be used for the expansion at the top.
     * @param c
     * @param rfc
     * @param expansion
     * @return
     */
    private static int[] upExpBand(Circuit c, RequestForConnection rfc, int expansion){

        List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute());

        int[] bandFreeAdjSuperior = IntersectionFreeSpectrum.bandAdjacentSuperior(c.getSpectrumAssigned(), composition);

        int upExpBand[] = null;
        if (expansion > 0) { // Expansion up
            upExpBand = new int[2];

            upExpBand[0] = bandFreeAdjSuperior[0];
            upExpBand[1] = upExpBand[0] + expansion - 1;
        }

        return upExpBand;
    }

}
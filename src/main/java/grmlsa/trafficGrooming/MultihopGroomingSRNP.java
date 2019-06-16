package grmlsa.trafficGrooming;

import grmlsa.SRNP;
import network.Circuit;
import network.ControlPlane;
import network.Node;
import network.Pair;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

/**
 * This class represents a Multihop Traffic Grooming algorithm whith the spectrum reservation scheme presented in "Dynamic Traf?c Grooming in Sliceable Bandwidth-Variable Transponder-Enabled Elastic Optical Networks".
 *
 * Extends this class to implement diferent trafic grooming policies.
 */
public abstract class MultihopGroomingSRNP implements TrafficGroomingAlgorithmInterface {
    protected static int microRegionsDeep = 2;
    protected int maxVirtualHops = 3;
    protected HashMap<String, HashMap<String, ArrayList<MultihopSolution>>> virtualRouting;
    protected HashMap<String, ArrayList<String>> microRegions;

    private SRNP srnp;


    private void initVirtualRouting(ControlPlane cp) {
        this.virtualRouting = new HashMap();
        Iterator it1 = cp.getMesh().getNodeList().iterator();

        while(it1.hasNext()) {
            String src = ((Node)it1.next()).getName();
            this.virtualRouting.put(src, new HashMap());
            Iterator it2 = cp.getMesh().getNodeList().iterator();

            while(it2.hasNext()) {
                String dst = ((Node)it2.next()).getName();
                ((HashMap)this.virtualRouting.get(src)).put(dst, new ArrayList());
            }
        }

    }

    private void initMicroRegions(ControlPlane cp) {
        this.microRegions = new HashMap();
        Iterator it1 = cp.getMesh().getNodeList().iterator();

        while(it1.hasNext()) {
            Node n = (Node)it1.next();
            this.microRegions.put(n.getName(), this.computeMicroRegion(cp, n));
        }

    }

    private ArrayList<String> computeMicroRegion(ControlPlane cp, Node r) {
        ArrayList<String> res = new ArrayList();
        ArrayList<Node> F = new ArrayList();
        F.add(r);

        label32:
        for(int i = 0; i < microRegionsDeep; ++i) {
            ArrayList<Node> aux = new ArrayList();
            Iterator var7 = F.iterator();

            while(true) {
                Node n;
                do {
                    if (!var7.hasNext()) {
                        F = aux;
                        continue label32;
                    }

                    n = (Node)var7.next();
                } while(res.contains(n.getName()));

                res.add(n.getName());
                Iterator var9 = cp.getMesh().getAdjacents(n).iterator();

                while(var9.hasNext()) {
                    Node n2 = (Node)var9.next();
                    aux.add(n2);
                }
            }
        }

        res.remove(r.getName());
        return res;
    }

    private static boolean hasVirtualLoop(ArrayList<Circuit> sol) {
        HashSet<String> nAux = new HashSet();
        nAux.add(((Circuit)sol.get(0)).getPair().getSource().getName());
        Iterator var2 = sol.iterator();

        while(var2.hasNext()) {
            Circuit c = (Circuit)var2.next();
            if (nAux.contains(c.getPair().getDestination().getName())) {
                return true;
            }

            nAux.add(c.getPair().getDestination().getName());
        }

        return false;
    }

    /**
     * updates the virtual routes when a new circuit is stabilished.
     * @param circuit
     */
    private void addNewCircuitVirtualRouting(Circuit circuit) {
        String dst = circuit.getPair().getDestination().getName();
        String src = circuit.getPair().getSource().getName();
        MultihopGroomingSRNP.MultihopSolution al = new MultihopGroomingSRNP.MultihopSolution();
        al.virtualRoute.add(circuit);
        al.src = src;
        al.dst = dst;
        ((ArrayList)((HashMap)this.virtualRouting.get(src)).get(dst)).add(al);
        ArrayList<MultihopGroomingSRNP.MultihopSolution> aalAux = new ArrayList();
        aalAux.add(al);
        Iterator it = ((HashMap)this.virtualRouting.get(dst)).values().iterator();

        Iterator it2;
        while(it.hasNext()) {
            it2 = ((ArrayList)it.next()).iterator();

            while(it2.hasNext()) {
                MultihopGroomingSRNP.MultihopSolution clone = ((MultihopGroomingSRNP.MultihopSolution)it2.next()).clone();
                clone.virtualRoute.add(0, circuit);
                clone.src = circuit.getSource().getName();
                if (clone.virtualRoute.size() < this.maxVirtualHops && !hasVirtualLoop(clone.virtualRoute)) {
                    ((ArrayList)((HashMap)this.virtualRouting.get(clone.src)).get(clone.dst)).add(clone);
                    aalAux.add(clone);
                }
            }
        }

        it2 = this.virtualRouting.keySet().iterator();

        while(it2.hasNext()) {
            String sA = (String)it2.next();
            Iterator it3 = ((ArrayList)((HashMap)this.virtualRouting.get(sA)).get(src)).iterator();

            while(it3.hasNext()) {
                MultihopGroomingSRNP.MultihopSolution c1 = (MultihopGroomingSRNP.MultihopSolution)it3.next();
                Iterator it4 = aalAux.iterator();

                while(it4.hasNext()) {
                    MultihopGroomingSRNP.MultihopSolution cAux = c1.clone();
                    MultihopGroomingSRNP.MultihopSolution c2 = ((MultihopGroomingSRNP.MultihopSolution)it4.next()).clone();
                    cAux.virtualRoute.addAll(c2.virtualRoute);
                    cAux.dst = c2.dst;
                    if (cAux.virtualRoute.size() < this.maxVirtualHops && !hasVirtualLoop(cAux.virtualRoute)) {
                        ((ArrayList)((HashMap)this.virtualRouting.get(cAux.src)).get(cAux.dst)).add(cAux);
                    }
                }
            }
        }

    }

    /**
     * updates the virtual routes when a circuit is finished.
     * @param circuit
     */
    private void removeCircuitVirtualRouting(Circuit circuit) {
        Set<String> srcs = this.virtualRouting.keySet();
        Iterator var3 = srcs.iterator();

        while(var3.hasNext()) {
            String src = (String)var3.next();
            Set<String> dsts = ((HashMap)this.virtualRouting.get(src)).keySet();
            Iterator var6 = dsts.iterator();

            while(var6.hasNext()) {
                String dst = (String)var6.next();
                ArrayList<MultihopGroomingSRNP.MultihopSolution> virtRoutes = (ArrayList)((HashMap)this.virtualRouting.get(src)).get(dst);
                ArrayList<MultihopGroomingSRNP.MultihopSolution> newVR = new ArrayList();
                Iterator var10 = virtRoutes.iterator();

                while(var10.hasNext()) {
                    MultihopGroomingSRNP.MultihopSolution vr = (MultihopGroomingSRNP.MultihopSolution)var10.next();
                    if (!vr.virtualRoute.contains(circuit)) {
                        newVR.add(vr);
                    }
                }

                ((HashMap)this.virtualRouting.get(src)).put(dst, newVR);
            }
        }

    }

    /**
     * gets all virtual routes avaliables for a specific request.
     * @param rfc
     * @return
     */
    private ArrayList<MultihopGroomingSRNP.MultihopSolution> allVirtualRouting(RequestForConnection rfc) {
        ArrayList<MultihopGroomingSRNP.MultihopSolution> allSolutions = new ArrayList();
        Iterator var3 = ((ArrayList)((HashMap)this.virtualRouting.get(rfc.getPair().getSource().getName())).get(rfc.getPair().getDestination().getName())).iterator();

        while(var3.hasNext()) {
            MultihopGroomingSRNP.MultihopSolution ms = (MultihopGroomingSRNP.MultihopSolution)var3.next();
            allSolutions.add(ms.clone());
        }

        var3 = ((ArrayList)this.microRegions.get(rfc.getPair().getDestination().getName())).iterator();

        while(var3.hasNext()) {
            String nearNode = (String)var3.next();
            ArrayList<MultihopGroomingSRNP.MultihopSolution> multihopSolutions = (ArrayList)((HashMap)this.virtualRouting.get(rfc.getPair().getSource().getName())).get(nearNode);
            Iterator var7 = multihopSolutions.iterator();

            while(var7.hasNext()) {
                MultihopGroomingSRNP.MultihopSolution ms = (MultihopGroomingSRNP.MultihopSolution)var7.next();
                MultihopGroomingSRNP.MultihopSolution msAux = ms.clone();
                msAux.needsComplement = true;
                msAux.pairComplement = new Pair(((Circuit)msAux.virtualRoute.get(msAux.virtualRoute.size() - 1)).getDestination(), rfc.getPair().getDestination());
                msAux.dst = rfc.getPair().getDestination().getName();
                allSolutions.add(msAux);
            }
        }

        MultihopGroomingSRNP.MultihopSolution ms = new MultihopGroomingSRNP.MultihopSolution();
        ms.src = rfc.getPair().getSource().getName();
        ms.dst = rfc.getPair().getDestination().getName();
        ms.virtualRoute = new ArrayList();
        ms.needsComplement = true;
        ms.pairComplement = rfc.getPair();
        allSolutions.add(ms);
        return allSolutions;
    }

    /**
     * defines how circuits will be expanded.
     * @param numMoreSlots
     * @param numLowerFreeSlots
     * @param numUpperFreeSlots
     * @return
     */
    protected static int[] decideToExpand(int numMoreSlots, int numLowerFreeSlots, int numUpperFreeSlots) {
        int[] res = new int[2];
        if (numLowerFreeSlots >= numMoreSlots) {
            res[0] = numMoreSlots;
            res[1] = 0;
        } else {
            res[0] = numLowerFreeSlots;
            res[1] = numMoreSlots - numLowerFreeSlots;
        }

        return res;
    }

    private Circuit complementSolution(MultihopGroomingSRNP.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Node s = ms.pairComplement.getSource();
        Node d = ms.pairComplement.getDestination();
        Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));
        if (!srnp.establishCircuit(newCircuit)) {
            return null;
        } else {
            newCircuit.removeRequest(rfc);
            rfc.setCircuit(new ArrayList());
            return newCircuit;
        }
    }

    private boolean hasSuficientResidualCapacity(MultihopGroomingSRNP.MultihopSolution ms, RequestForConnection rfc) {
        boolean hasSuficientResitualCapacity = true;

        Circuit circuit;
        for(Iterator var4 = ms.virtualRoute.iterator(); var4.hasNext(); hasSuficientResitualCapacity = hasSuficientResitualCapacity && circuit.getResidualCapacity() > rfc.getRequiredBandwidth()) {
            circuit = (Circuit)var4.next();
        }

        return hasSuficientResitualCapacity;
    }

    private boolean canBeExpanded(Circuit c, RequestForConnection rfc, ControlPlane cp) {
        int[] exp = Grooming.circuitExpansiveness(c, cp);
        int circExCap = exp[0] + exp[1];
        int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
        return circExCap >= slotsNeeded;
    }

    private boolean aplySolution(MultihopGroomingSRNP.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Circuit newCircuit = null;
        rfc.setCircuit(new ArrayList());
        if (ms.needsComplement) {
            newCircuit = this.complementSolution(ms, rfc, cp);
            if (newCircuit == null) {
                return false;
            }

            ms.virtualRoute.add(newCircuit);
            ms.needsComplement = false;
        }

        boolean hasSuficientResitualCapacity = this.hasSuficientResidualCapacity(ms, rfc);
        if (hasSuficientResitualCapacity) {
            Iterator var16 = ms.virtualRoute.iterator();

            while(var16.hasNext()) {
                Circuit circuit = (Circuit)var16.next();
                circuit.getRequests().add(rfc);
            }

            rfc.setCircuit(ms.virtualRoute);
            if (newCircuit != null) {
                this.addNewCircuitVirtualRouting(newCircuit);
            }

            return true;
        } else {
            boolean canBeExpanded = true;
            List<Integer> upExps = new ArrayList();
            List<Integer> downExps = new ArrayList();

            int i;
            for(i = 0; i < ms.virtualRoute.size(); ++i) {
                Circuit c = (Circuit)ms.virtualRoute.get(i);
                if (!this.canBeExpanded(c, rfc, cp)) {
                    canBeExpanded = false;
                    break;
                }

                int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
                if (slotsNeeded > 0) {
                    List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute(), c.getGuardBand());
                    int bandFreeAdjInferior = IntersectionFreeSpectrum.freeSlotsDown(c.getSpectrumAssigned(), composition, c.getGuardBand());
                    int bandFreeAdjSuperior = IntersectionFreeSpectrum.freeSlotsUpper(c.getSpectrumAssigned(), composition, c.getGuardBand());
                    int[] expansion = decideToExpand(slotsNeeded, bandFreeAdjInferior, bandFreeAdjSuperior);
                    downExps.add(expansion[0]);
                    upExps.add(expansion[1]);
                    if (!cp.expandCircuit(c, expansion[0], expansion[1])) {
                        canBeExpanded = false;
                        break;
                    }

                    c.addRequest(rfc);
                    rfc.getCircuits().add(c);
                } else {
                    c.addRequest(rfc);
                    rfc.getCircuits().add(c);
                    downExps.add(0);
                    upExps.add(0);
                }
            }

            if (canBeExpanded) {
                if (newCircuit != null) {
                    this.addNewCircuitVirtualRouting(newCircuit);
                }

                return true;
            } else {
                --i;

                while(i >= 0) {
                    cp.retractCircuit((Circuit)ms.virtualRoute.get(i), (Integer)downExps.get(i), (Integer)upExps.get(i));
                    ((Circuit)ms.virtualRoute.get(i)).removeRequest(rfc);
                    --i;
                }

                if (newCircuit != null) {
                    cp.releaseCircuit(newCircuit);
                }

                rfc.setCircuit(new ArrayList());
                return false;
            }
        }
    }

    public boolean searchCircuitsForGrooming(final RequestForConnection rfc, final ControlPlane cp) throws Exception {
        if (this.virtualRouting == null) {
            this.initVirtualRouting(cp);
        }
        if (this.microRegions == null) {
            this.initMicroRegions(cp);
        }
        if(srnp==null){
            srnp = new SRNP(cp);
        }

        ArrayList<MultihopGroomingSRNP.MultihopSolution> multihopSolutions = this.allVirtualRouting(rfc);
        this.avMS(multihopSolutions, rfc, cp);
        Collections.sort(multihopSolutions, new Comparator<MultihopGroomingSRNP.MultihopSolution>() {
            public int compare(MultihopGroomingSRNP.MultihopSolution o1, MultihopGroomingSRNP.MultihopSolution o2) {
                Double c1 = MultihopGroomingSRNP.this.costFunction(o1, rfc, cp);
                Double c2 = MultihopGroomingSRNP.this.costFunction(o2, rfc, cp);
                int res = c1.compareTo(c2);
                return res;
            }
        });
        Iterator var4 = multihopSolutions.iterator();

        MultihopGroomingSRNP.MultihopSolution ms;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            ms = (MultihopGroomingSRNP.MultihopSolution)var4.next();
        } while(!this.aplySolution(ms, rfc, cp));

        return true;
    }

    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {
        Iterator var3 = rfc.getCircuits().iterator();

        while(var3.hasNext()) {
            Circuit circuit = (Circuit)var3.next();
            if (circuit.getRequests().size() == 1) {
                try {
                    cp.releaseCircuit(circuit);
                    this.removeCircuitVirtualRouting(circuit);
                } catch (Exception var9) {
                    throw var9;
                }
            } else {
                srnp.computeResidualCapacity();
                Double actualReserve = srnp.getReservesByNode().get(circuit.getPair().getName());
                Double retract = actualReserve - srnp.getReservationTarget();
                Double retractInThisCirc;
                if(circuit.getResidualCapacity()>retract){
                    retractInThisCirc = retract;
                }else{
                    retractInThisCirc = circuit.getResidualCapacity();
                }
                if(retractInThisCirc>0){//retract the circuit

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

                circuit.removeRequest(rfc);
            }
        }

    }

    protected abstract double costFunction(MultihopGroomingSRNP.MultihopSolution var1, RequestForConnection var2, ControlPlane var3);

    private boolean badMS(MultihopGroomingSRNP.MultihopSolution sol, RequestForConnection rfc) {
        boolean res = true;
        if (sol.virtualRoute.size() == 0) {
            res = res && sol.needsComplement;
            res = res && sol.pairComplement.getSource().getName().equals(rfc.getPair().getSource().getName());
            res = res && sol.pairComplement.getDestination().getName().equals(rfc.getPair().getDestination().getName());
            return !res;
        } else {
            int i;
            for(i = 0; i < sol.virtualRoute.size() - 1; ++i) {
                res = res && ((Circuit)sol.virtualRoute.get(i)).getDestination().getName().equals(((Circuit)sol.virtualRoute.get(i + 1)).getSource().getName());
            }

            res = res && ((Circuit)sol.virtualRoute.get(0)).getSource().getName().equals(rfc.getPair().getSource().getName());
            if (!sol.needsComplement) {
                res = res && ((Circuit)sol.virtualRoute.get(sol.virtualRoute.size() - 1)).getDestination().getName().equals(rfc.getPair().getDestination().getName());
            } else {
                res = res && sol.pairComplement.getSource().getName().equals(((Circuit)sol.virtualRoute.get(i)).getDestination().getName());
                res = res && sol.pairComplement.getDestination().getName().equals(rfc.getPair().getDestination().getName());
            }

            return !res;
        }
    }

    private void avMS(ArrayList<MultihopGroomingSRNP.MultihopSolution> ams, RequestForConnection rfc, ControlPlane cp) {
        double physicalHops = -1.0D;
        double virtualHops = -1.0D;
        double spectrumUtilization = -1.0D;
        double minSNR = -1.0D;
        double meanSNR = -1.0D;
        int transceivers = 1;

        MultihopGroomingSRNP.MultihopSolution ms;
        MultihopGroomingSRNP.MultihopSolutionStatistics mss;
        Iterator var15;
        for(var15 = ams.iterator(); var15.hasNext(); ms.statistics = mss) {
            ms = (MultihopGroomingSRNP.MultihopSolution)var15.next();
            mss = new MultihopGroomingSRNP.MultihopSolutionStatistics(ms, rfc, cp);
            if (mss.physicalHops > physicalHops) {
                physicalHops = mss.physicalHops;
            }

            if (mss.virtualHops > virtualHops) {
                virtualHops = mss.virtualHops;
            }

            if (mss.spectrumUtilization > spectrumUtilization) {
                spectrumUtilization = mss.spectrumUtilization;
            }

            if (mss.minSNR > minSNR) {
                minSNR = mss.minSNR;
            }

            if (mss.meanSNR > meanSNR) {
                meanSNR = mss.meanSNR;
            }

            if (mss.transceivers > transceivers) {
                transceivers = mss.transceivers;
            }
        }

        for(var15 = ams.iterator(); var15.hasNext(); ms.statistics.transceivers /= transceivers) {
            ms = (MultihopGroomingSRNP.MultihopSolution)var15.next();
            ms.statistics.physicalHops /= physicalHops;
            ms.statistics.virtualHops /= virtualHops;
            ms.statistics.spectrumUtilization /= spectrumUtilization;
            ms.statistics.minSNR /= minSNR;
            ms.statistics.meanSNR /= meanSNR;
        }

    }

    protected static class MultihopSolutionStatistics {
        public double physicalHops = 0.0D;
        public double virtualHops;
        public double spectrumUtilization;
        public double minSNR;
        public double meanSNR;
        public int transceivers;

        public MultihopSolutionStatistics(MultihopGroomingSRNP.MultihopSolution sol, RequestForConnection rfc, ControlPlane cp) {
            this.virtualHops = (double)sol.virtualRoute.size();
            this.spectrumUtilization = 0.0D;
            this.minSNR = 1.0E8D;
            this.meanSNR = 0.0D;
            this.transceivers = 0;

            Iterator iterator;
            double snr;
            iterator = sol.virtualRoute.iterator();
            while(iterator.hasNext()) {
                Circuit circuit = (Circuit)iterator.next();
                this.physicalHops += (double)circuit.getRoute().getHops();
                circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth());
                this.spectrumUtilization += (double)(circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * circuit.getRoute().getHops());
                snr = circuit.getSNR();
                if (snr < this.minSNR) {
                    this.minSNR = snr;
                }
                this.meanSNR += snr;
            }

            if (sol.needsComplement) {
                Node s = sol.pairComplement.getSource();
                Node d = sol.pairComplement.getDestination();
                Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));

                try {
                    if (!cp.establishCircuit(newCircuit)) {
                        this.physicalHops = 1.0E8D;
                        this.meanSNR = 1.0E8D;
                    } else {
                        newCircuit.removeRequest(rfc);
                        this.physicalHops += (double)newCircuit.getRoute().getHops();
                        ++this.virtualHops;
                        this.spectrumUtilization += (double)(newCircuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * newCircuit.getRoute().getHops());
                        snr = newCircuit.getSNR();
                        if (snr < this.minSNR) {
                            this.minSNR = snr;
                        }

                        this.meanSNR += snr;
                        ++this.transceivers;
                        cp.releaseCircuit(newCircuit);
                    }
                } catch (Exception var9) {
                    var9.printStackTrace();
                }

                rfc.getCircuits().remove(newCircuit);
            }

            this.meanSNR /= this.virtualHops;
        }
    }

    protected static class MultihopSolution {
        public String src;
        public String dst;
        public ArrayList<Circuit> virtualRoute = new ArrayList();
        public boolean needsComplement = false;
        public Pair pairComplement = null;
        public MultihopGroomingSRNP.MultihopSolutionStatistics statistics = null;

        protected MultihopSolution() {
        }

        public MultihopGroomingSRNP.MultihopSolution clone() {
            MultihopGroomingSRNP.MultihopSolution n = new MultihopGroomingSRNP.MultihopSolution();
            n.src = this.src + "";
            n.dst = this.dst + "";
            n.virtualRoute = (ArrayList)this.virtualRoute.clone();
            n.needsComplement = this.needsComplement;
            return n;
        }
    }
}




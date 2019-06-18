package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import network.Node;
import network.Pair;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

public abstract class MultihopGrooming implements TrafficGroomingAlgorithmInterface {
    private static int microRegionsDeep = 2;
    private int maxVirtualHops = 3;
    private HashMap<String, HashMap<String, ArrayList<MultihopSolution>>> virtualRouting;
    private HashMap<String, ArrayList<String>> microRegions;

    private void initVirtualRouting(ControlPlane cp) {
        this.virtualRouting = new HashMap<>();

        for (Node o : cp.getMesh().getNodeList()) {
            String src = o.getName();
            this.virtualRouting.put(src, new HashMap<>());

            for (Node node : cp.getMesh().getNodeList()) {
                String dst = (node).getName();
                this.virtualRouting.get(src).put(dst, new ArrayList<>());
            }
        }

    }

    private void initMicroRegions(ControlPlane cp) {
        this.microRegions = new HashMap<>();

        for (Node n : cp.getMesh().getNodeList()) {
            this.microRegions.put(n.getName(), computeMicroRegion(cp, n,microRegionsDeep));
        }

    }

    private ArrayList<String> computeMicroRegion(ControlPlane cp, Node n, int deep){
        HashSet<String> mr = computeMicroRegionAux(cp, n, deep);
        mr.remove(n.getName());
        return new ArrayList<>(mr);
    }

    private HashSet<String> computeMicroRegionAux(ControlPlane cp, Node n, int deep) {
        HashSet<String> res = new HashSet<>();
        res.add(n.getName());
        if(deep>0){
            for (Node v : cp.getMesh().getAdjacents(n)){
                res.addAll(computeMicroRegionAux(cp,v,deep-1));
            }
        }
        return res;
    }

    private static boolean makeLoop(ArrayList<Circuit> sol, Circuit newCirc){

        for (Circuit c : sol) {
            if (c.getSource().getName().equals(newCirc.getSource().getName())) return true;
            if (c.getSource().getName().equals(newCirc.getDestination().getName())) return true;
        }

        return false;
    }

    private static boolean hasVirtualLoop(ArrayList<Circuit> sol) {
        HashSet<String> nAux = new HashSet<>();
        nAux.add(sol.get(0).getPair().getSource().getName());

        for (Circuit c : sol) {
            if (nAux.contains(c.getPair().getDestination().getName())) {
                return true;
            }

            nAux.add(c.getPair().getDestination().getName());
        }

        return false;
    }

    private void addNewCircuitVirtualRouting(Circuit circuit) {
        String dst = circuit.getPair().getDestination().getName();
        String src = circuit.getPair().getSource().getName();
        MultihopGrooming.MultihopSolution al = new MultihopGrooming.MultihopSolution();
        al.virtualRoute.add(circuit);
        al.src = src;
        al.dst = dst;
        this.virtualRouting.get(src).get(dst).add(al);
        ArrayList<MultihopGrooming.MultihopSolution> aalAux = new ArrayList<>();
        aalAux.add(al);
        Iterator<ArrayList<MultihopSolution>> it = this.virtualRouting.get(dst).values().iterator();

        Iterator it2;
        while(it.hasNext()) {
            it2 = (it.next()).iterator();

            while(it2.hasNext()) {
                MultihopSolution ms = (MultihopSolution) it2.next();
                if(ms.virtualRoute.size()==this.maxVirtualHops) continue;
                if(makeLoop(ms.virtualRoute,circuit)) continue;
                MultihopSolution clone = ms.clone();
                clone.virtualRoute.add(0, circuit);
                clone.src = circuit.getSource().getName();
                this.virtualRouting.get(clone.src).get(clone.dst).add(clone);
                aalAux.add(clone);
            }
        }

        it2 = this.virtualRouting.keySet().iterator();

        while(it2.hasNext()) {
            String sA = (String)it2.next();

            for (MultihopSolution c1 : this.virtualRouting.get(sA).get(src)) {

                for (MultihopSolution ms4 : aalAux) {
                    if (c1.virtualRoute.size() + ms4.virtualRoute.size() > maxVirtualHops) continue;
                    MultihopSolution cAux = c1.clone();
                    MultihopSolution c2 = ms4.clone();
                    cAux.virtualRoute.addAll(c2.virtualRoute);
                    cAux.dst = c2.dst;
                    if (!hasVirtualLoop(cAux.virtualRoute)) {
                        this.virtualRouting.get(cAux.src).get(cAux.dst).add(cAux);
                    }
                }
            }
        }

    }

    protected void removeCircuitVirtualRouting(Circuit circuit) {
        Set<String> srcs = this.virtualRouting.keySet();

        for (String src : srcs) {
            Set<String> dsts = this.virtualRouting.get(src).keySet();

            for (String dst : dsts) {
                ArrayList<MultihopSolution> virtRoutes =  this.virtualRouting.get(src).get(dst);
                ArrayList<MultihopSolution> newVR = new ArrayList<>();

                for (MultihopSolution vr : virtRoutes) {
                    if (!vr.virtualRoute.contains(circuit)) {
                        newVR.add(vr);
                    }
                }

                this.virtualRouting.get(src).put(dst, newVR);
            }
        }

    }

    private ArrayList<MultihopGrooming.MultihopSolution> allVirtualRouting(RequestForConnection rfc) {
        ArrayList<MultihopGrooming.MultihopSolution> allSolutions = new ArrayList<>();
        Iterator var3 = this.virtualRouting.get(rfc.getPair().getSource().getName()).get(rfc.getPair().getDestination().getName()).iterator();

        while(var3.hasNext()) {
            MultihopGrooming.MultihopSolution ms = (MultihopGrooming.MultihopSolution)var3.next();
            allSolutions.add(ms.clone());
        }

        var3 = this.microRegions.get(rfc.getPair().getDestination().getName()).iterator();

        while(var3.hasNext()) {//for TGS who needs complement
            String nearNode = (String)var3.next();
            ArrayList<MultihopGrooming.MultihopSolution> multihopSolutions = this.virtualRouting.get(rfc.getPair().getSource().getName()).get(nearNode);

            for (MultihopSolution ms : multihopSolutions) {
                if (ms.virtualRoute.size() < this.maxVirtualHops) {
                    MultihopSolution msAux = ms.clone();
                    msAux.needsComplement = true;
                    msAux.pairComplement = new Pair(msAux.virtualRoute.get(msAux.virtualRoute.size() - 1).getDestination(), rfc.getPair().getDestination());
                    msAux.dst = rfc.getPair().getDestination().getName();
                    allSolutions.add(msAux);
                }
            }
        }

        MultihopGrooming.MultihopSolution ms = new MultihopGrooming.MultihopSolution();
        ms.src = rfc.getPair().getSource().getName();
        ms.dst = rfc.getPair().getDestination().getName();
        ms.virtualRoute = new ArrayList<>();
        ms.needsComplement = true;
        ms.pairComplement = rfc.getPair();
        allSolutions.add(ms);
        return allSolutions;
    }

    private static int[] decideToExpand(int numMoreSlots, int numLowerFreeSlots, int numUpperFreeSlots) {
        int[] res = new int[2];
        if (numLowerFreeSlots >= numMoreSlots) {
            res[0] = numMoreSlots;
            //res[1] = 0;
        } else {
            res[0] = numLowerFreeSlots;
            res[1] = numMoreSlots - numLowerFreeSlots;
        }

        return res;
    }

    protected Circuit complementSolution(MultihopGrooming.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Node s = ms.pairComplement.getSource();
        Node d = ms.pairComplement.getDestination();
        Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));
        if (!cp.establishCircuit(newCircuit)) {
            return null;
        } else {
            newCircuit.removeRequest(rfc);
            rfc.setCircuit(new ArrayList<>());
            return newCircuit;
        }
    }

    private boolean hasSuficientResidualCapacity(MultihopGrooming.MultihopSolution ms, RequestForConnection rfc) {
        boolean hasSuficientResitualCapacity = true;

        Circuit circuit;
        for(Iterator<Circuit> var4 = ms.virtualRoute.iterator(); var4.hasNext(); hasSuficientResitualCapacity = hasSuficientResitualCapacity && circuit.getResidualCapacity() > rfc.getRequiredBandwidth()) {
            circuit = var4.next();
        }

        return hasSuficientResitualCapacity;
    }

    private boolean canBeExpanded(Circuit c, RequestForConnection rfc) {
        int[] exp = Grooming.circuitExpansiveness(c);
        int circExCap = exp[0] + exp[1];
        int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
        return circExCap >= slotsNeeded;
    }

    private boolean aplySolution(MultihopGrooming.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Circuit newCircuit = null;
        rfc.setCircuit(new ArrayList<>());
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

            for (Circuit circuit : ms.virtualRoute) {
                circuit.getRequests().add(rfc);
            }

            rfc.setCircuit(ms.virtualRoute);
            if (newCircuit != null) {
                this.addNewCircuitVirtualRouting(newCircuit);
            }

            return true;
        } else {
            boolean canBeExpanded = true;
            List<Integer> upExps = new ArrayList<>();
            List<Integer> downExps = new ArrayList<>();

            int i;
            for(i = 0; i < ms.virtualRoute.size(); ++i) {
                Circuit c = (Circuit)ms.virtualRoute.get(i);
                if (!this.canBeExpanded(c, rfc)) {
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

                rfc.setCircuit(new ArrayList<>());
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

        ArrayList<MultihopGrooming.MultihopSolution> multihopSolutions = this.allVirtualRouting(rfc);
        this.avMS(multihopSolutions, rfc, cp);
        multihopSolutions.sort(new Comparator<MultihopSolution>() {
            public int compare(MultihopSolution o1, MultihopSolution o2) {
                Double c1 = MultihopGrooming.this.costFunction(o1, rfc, cp);
                Double c2 = MultihopGrooming.this.costFunction(o2, rfc, cp);
                return c1.compareTo(c2);
            }
        });
        Iterator<MultihopSolution> var4 = multihopSolutions.iterator();

        MultihopGrooming.MultihopSolution ms;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            ms = var4.next();
        } while(!this.aplySolution(ms, rfc, cp));

        return true;
    }

    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {

        for (Circuit circuit : rfc.getCircuits()) {
            if (circuit.getRequests().size() == 1) {
                cp.releaseCircuit(circuit);
                this.removeCircuitVirtualRouting(circuit);
            } else {
                int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() - rfc.getRequiredBandwidth());
                int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
                int release = numCurrentSlots - numFinalSlots;
                int[] releaseBand = new int[2];
                if (release != 0) {
                    releaseBand[1] = circuit.getSpectrumAssigned()[1];
                    releaseBand[0] = releaseBand[1] - release + 1;
                    cp.retractCircuit(circuit, 0, release);
                }

                circuit.removeRequest(rfc);
            }
        }

    }

    protected abstract double costFunction(MultihopGrooming.MultihopSolution var1, RequestForConnection var2, ControlPlane var3);

    /**
     * Compute the statistics for each traffic grooming solution.
     * @param ams traffic grooming solutions
     * @param rfc request to be served
     * @param cp control plane
     */
    private void avMS(ArrayList<MultihopGrooming.MultihopSolution> ams, RequestForConnection rfc, ControlPlane cp) {
        double physicalHops = -1.0D;
        double virtualHops = -1.0D;
        double spectrumUtilization = -1.0D;
        double minSNR = -1.0D;
        double meanSNR = -1.0D;
        int transceivers = 1;

        MultihopGrooming.MultihopSolution ms;
        MultihopGrooming.MultihopSolutionStatistics mss;
        Iterator<MultihopSolution> it;
        MSSOptimizator msso = new MSSOptimizator();
        for(it = ams.iterator(); it.hasNext(); ms.statistics = mss) {
            ms = it.next(); //for each TGS
            mss = new MultihopGrooming.MultihopSolutionStatistics(ms, rfc, cp, msso); //compute statistics
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

        for(it = ams.iterator(); it.hasNext(); ms.statistics.transceivers /= transceivers) {
            ms = it.next();
            ms.statistics.physicalHops /= physicalHops;
            ms.statistics.virtualHops /= virtualHops;
            ms.statistics.spectrumUtilization /= spectrumUtilization;
            ms.statistics.minSNR /= minSNR;
            ms.statistics.meanSNR /= meanSNR;
        }

    }

    protected static class MultihopSolutionStatistics {

        double physicalHops = 0.0D;
        double virtualHops;
        double spectrumUtilization;
        double minSNR;
        double meanSNR;
        int transceivers;
        double SNRImpact;

        MultihopSolutionStatistics(MultihopGrooming.MultihopSolution sol, RequestForConnection rfc, ControlPlane cp, MSSOptimizator msso) {
            this.virtualHops = (double)sol.virtualRoute.size();
            this.spectrumUtilization = 0.0D;
            this.minSNR = 1.0E8D;
            this.meanSNR = 0.0D;
            this.transceivers = 0;
            this.SNRImpact = 0;

            Iterator<Circuit> iterator;
            double snr;
            iterator = sol.virtualRoute.iterator();
            while(iterator.hasNext()) {
                Circuit circuit = iterator.next();
                this.physicalHops += (double)circuit.getRoute().getHops();
                circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth());
                this.spectrumUtilization += (double)(circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * circuit.getRoute().getHops());
                snr = circuit.getSNR();
                if (snr < this.minSNR) {
                    this.minSNR = snr;
                }
                this.meanSNR += snr;
                if(msso.oSNRImpactPerCircuit.get(circuit)==null){
                    msso.oSNRImpactPerCircuit.put(circuit,cp.computesImpactOnSNROther(circuit));
                }
                this.SNRImpact += msso.oSNRImpactPerCircuit.get(circuit);
            }

            if (sol.needsComplement) {
                if(msso.oCanEstabilish.get(sol.pairComplement.toString())==null){
                    Node s = sol.pairComplement.getSource();
                    Node d = sol.pairComplement.getDestination();
                    Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));

                    try {
                        if (!cp.establishCircuit(newCircuit)) {
                            msso.oCanEstabilish.put(sol.pairComplement.toString(),false);
                            msso.oPhysicalHops.put(sol.pairComplement.toString(),1.0E8D);
                            msso.oSNR.put(sol.pairComplement.toString(),1.0E8D);
                            msso.oSNRImpact.put(sol.pairComplement.toString(),1.0E8D);
                        } else {
                            newCircuit.removeRequest(rfc);
                            msso.oPhysicalHops.put(sol.pairComplement.toString(),(double)newCircuit.getRoute().getHops());
                            msso.oCanEstabilish.put(sol.pairComplement.toString(),true);
                            msso.oSpectrumUtilization.put(sol.pairComplement.toString(), (double)(newCircuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * newCircuit.getRoute().getHops()));
                            msso.oSNR.put(sol.pairComplement.toString(),newCircuit.getSNR());
                            msso.oSNRImpact.put(sol.pairComplement.toString(),cp.computesImpactOnSNROther(newCircuit));
                            cp.releaseCircuit(newCircuit);
                        }
                    } catch (Exception var9) {
                        var9.printStackTrace();
                    }
                    rfc.getCircuits().remove(newCircuit);
                }
                if (!msso.oCanEstabilish.get(sol.pairComplement.toString())) {
                    this.physicalHops = 1.0E8D;
                    this.meanSNR = 1.0E8D;
                    this.SNRImpact = 1.0E8D;
                } else {
                    this.physicalHops += msso.oPhysicalHops.get(sol.pairComplement.toString());
                    ++this.virtualHops;
                    this.spectrumUtilization += msso.oSpectrumUtilization.get(sol.pairComplement.toString());
                    snr = msso.oSNR.get(sol.pairComplement.toString());
                    if (snr < this.minSNR) {
                        this.minSNR = snr;
                    }
                    this.meanSNR += snr;
                    this.SNRImpact += msso.oSNRImpact.get(sol.pairComplement.toString());
                    this.transceivers++;
                }
            }

            this.meanSNR /= this.virtualHops;
        }
    }

    public class MSSOptimizator{
        HashMap<String,Boolean> oCanEstabilish;
        HashMap<String,Double> oPhysicalHops;
        HashMap<String,Double> oSNR;
        HashMap<String,Double> oSNRImpact;
        HashMap<String,Double> oSpectrumUtilization;

        HashMap<Circuit,Double> oSNRImpactPerCircuit; //store the snrImpact per circuit.

        MSSOptimizator(){
            oCanEstabilish = new HashMap<>();
            oPhysicalHops = new HashMap<>();
            oSNR = new HashMap<>();
            oSNRImpact = new HashMap<>();
            oSpectrumUtilization = new HashMap<>();
            oSNRImpactPerCircuit = new HashMap<>();
        }
    }

    protected static class MultihopSolution {
        String src;
        String dst;
        ArrayList<Circuit> virtualRoute = new ArrayList<>();
        boolean needsComplement = false;
        Pair pairComplement = null;
        MultihopGrooming.MultihopSolutionStatistics statistics = null;

        @Override
        public MultihopGrooming.MultihopSolution clone() {
            MultihopSolution n = new MultihopSolution();
            n.src = this.src + "";
            n.dst = this.dst + "";
            n.virtualRoute = (ArrayList<Circuit>)this.virtualRoute.clone();
            n.needsComplement = this.needsComplement;
            return n;
        }
    }
}




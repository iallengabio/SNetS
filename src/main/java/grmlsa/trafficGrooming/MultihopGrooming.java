package grmlsa.trafficGrooming;

import grmlsa.Route;
import network.*;
import request.RequestForConnection;
import grmlsa.trafficGrooming.util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

public abstract class MultihopGrooming implements TrafficGroomingAlgorithmInterface {
    private static int microRegionsDeep = 2;
    private int maxVirtualHops = 3;
    private HashMap<String, HashMap<String, ArrayList<MultihopSolution>>> virtualRouting;
    private HashMap<String, HashSet<Node>> microRegions;
    private HashMap<Circuit,Double> oSNRImpactPerCircuit = new HashMap<>(); //store the snrImpact per circuit.

    private void initVirtualRouting(ControlPlane cp) {
        this.virtualRouting = new HashMap<>();

        for (Node o : cp.getMesh().getNodeList()) {
            String src = o.getName();
            this.virtualRouting.put(src, new HashMap<>());

            for (Node d : cp.getMesh().getNodeList()) {
                String dst = d.getName();
                this.virtualRouting.get(src).put(dst, new ArrayList<>());
                MultihopSolution ms = new MultihopSolution();
                ms.src = src;
                ms.dst = dst;
                ms.virtualRoute = new ArrayList<>();
                ms.needsComplement = true;
                ms.pairComplement = new Pair(o,d);
                this.virtualRouting.get(src).get(dst).add(ms);
            }
        }

    }

    private void initMicroRegions(ControlPlane cp) {
        this.microRegions = new HashMap<>();

        for (Node n : cp.getMesh().getNodeList()) {
            this.microRegions.put(n.getName(), computeMicroRegion(cp, n,microRegionsDeep));
        }

    }

    private HashSet<Node> computeMicroRegion(ControlPlane cp, Node n, int deep){
        HashSet<Node> mr = computeMicroRegionAux(cp, n, deep);
        mr.remove(n);
        return mr;
    }

    private HashSet<Node> computeMicroRegionAux(ControlPlane cp, Node n, int deep) {
        HashSet<Node> res = new HashSet<>();
        res.add(n);
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

    private static boolean hasVirtualLoop(MultihopSolution sol) {
        if(sol.virtualRoute.size()==0) return false;
        HashSet<String> nAux = new HashSet<>();

        nAux.add(sol.virtualRoute.get(0).getPair().getSource().getName());

        for (Circuit c : sol.virtualRoute) {
            if (nAux.contains(c.getPair().getDestination().getName())) {
                return true;
            }
            nAux.add(c.getPair().getDestination().getName());
        }
        if(sol.needsComplement && nAux.contains(sol.pairComplement.getDestination().getName())) return true;

        return false;
    }

    private void addNewCircuitVirtualRouting(Circuit circuit) {
        updateSNRImpact(circuit);
        String dst = circuit.getPair().getDestination().getName();
        String src = circuit.getPair().getSource().getName();
        MultihopSolution al = new MultihopSolution();
        al.virtualRoute.add(circuit);
        al.src = src;
        al.dst = dst;
        this.virtualRouting.get(src).get(dst).add(al);

        ArrayList<MultihopSolution> aalAux = new ArrayList<>();
        aalAux.add(al);

        //From the destination of the new circuit.
        for (ArrayList<MultihopSolution> multihopSolutions : this.virtualRouting.get(dst).values()) {
            for (MultihopSolution ms : multihopSolutions) {
                if(ms.virtualRoute.size()==0 && !microRegions.get(ms.src).contains(ms.pairComplement.getDestination())) continue;
                if (ms.virtualRoute.size() == this.maxVirtualHops) continue;
                if (makeLoop(ms.virtualRoute, circuit)) continue;
                MultihopSolution clone = ms.clone();
                clone.virtualRoute.add(0, circuit);
                clone.src = circuit.getSource().getName();
                this.virtualRouting.get(clone.src).get(clone.dst).add(clone);
                aalAux.add(clone);
            }
        }



        //Those who arrive at the origin of the new circuit.
        for (String sA : virtualRouting.keySet()) {
            ArrayList<MultihopSolution> msaux = virtualRouting.get(sA).get(src);
            for (MultihopSolution c1 : msaux) {
                if(c1.needsComplement) continue;
                for (MultihopSolution ms4 : aalAux) {
                    int vh = c1.virtualRoute.size() + ms4.virtualRoute.size();
                    if(ms4.needsComplement) vh++;
                    if (vh > maxVirtualHops) continue;
                    MultihopSolution cAux = c1.clone();
                    MultihopSolution c2 = ms4.clone();
                    cAux.virtualRoute.addAll(c2.virtualRoute);
                    cAux.dst = c2.dst;
                    cAux.needsComplement = c2.needsComplement;
                    cAux.pairComplement = c2.pairComplement;
                    if (!hasVirtualLoop(cAux)) {
                        this.virtualRouting.get(cAux.src).get(cAux.dst).add(cAux);
                    }
                }
            }
        }

    }

    /**
     * this method must be called when a circuit is added or removed.
     * @param c
     */
    private void updateSNRImpact(Circuit c){
        Route r = c.getRoute();
        for(Link l: r.getLinkList()){
            for(Circuit ci: l.getCircuitList()){
                oSNRImpactPerCircuit.remove(ci);
            }
        }
    }

    protected void removeCircuitVirtualRouting(Circuit circuit) {
        updateSNRImpact(circuit);
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

    private ArrayList<MultihopSolution> allVirtualRouting(RequestForConnection rfc) {
        ArrayList<MultihopSolution> allSolutions = new ArrayList<>();

        allSolutions = (ArrayList<MultihopSolution>) this.virtualRouting.get(rfc.getPair().getSource().getName()).get(rfc.getPair().getDestination().getName());

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

    protected Circuit complementSolution(MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
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

    private boolean hasSuficientResidualCapacity(MultihopSolution ms, RequestForConnection rfc) {
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

    private boolean aplySolution(MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        ms = ms.clone();
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
                    List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute());
                    int bandFreeAdjInferior = IntersectionFreeSpectrum.freeSlotsDown(c.getSpectrumAssigned(), composition);
                    int bandFreeAdjSuperior = IntersectionFreeSpectrum.freeSlotsUpper(c.getSpectrumAssigned(), composition);
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
                    cp.retractCircuit(ms.virtualRoute.get(i), downExps.get(i), upExps.get(i));
                    ms.virtualRoute.get(i).removeRequest(rfc);
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

    public boolean searchCircuitsForGrooming(RequestForConnection rfc, final ControlPlane cp) throws Exception {


        if (this.virtualRouting == null) {
            this.initVirtualRouting(cp);
        }

        if (this.microRegions == null) {
            this.initMicroRegions(cp);
        }

        //printVirtualRoute();

        ArrayList<MultihopSolution> multihopSolutions = this.allVirtualRouting(rfc);
        this.avMS(multihopSolutions, rfc, cp);
        multihopSolutions.sort(new Comparator<MultihopSolution>() {
            public int compare(MultihopSolution o1, MultihopSolution o2) {
                Double c1 = costFunction(o1, rfc, cp);
                Double c2 = costFunction(o2, rfc, cp);
                return c1.compareTo(c2);
            }
        });
        Iterator<MultihopSolution> var4 = multihopSolutions.iterator();

        MultihopSolution ms;
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

    protected abstract double costFunction(MultihopSolution var1, RequestForConnection var2, ControlPlane var3);

    /**
     * Compute the statistics for each traffic grooming solution.
     * @param ams traffic grooming solutions
     * @param rfc request to be served
     * @param cp control plane
     */
    private void avMS(ArrayList<MultihopSolution> ams, RequestForConnection rfc, ControlPlane cp) {
        double physicalHops = -1.0D;
        double virtualHops = -1.0D;
        double spectrumUtilization = -1.0D;
        double minSNR = -1.0D;
        double meanSNR = -1.0D;
        double snrImpact = -1.0D;
        int transceivers = 1;

        MultihopSolutionStatistics mss;
        MSSOptimizator msso = new MSSOptimizator();
        for(MultihopSolution ms: ams) {
            mss = new MultihopSolutionStatistics(ms, rfc, cp, msso); //compute statistics
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

            if(mss.SNRImpact > snrImpact){
                snrImpact = mss.SNRImpact;
            }

            if (mss.transceivers > transceivers) {
                transceivers = mss.transceivers;
            }
            ms.statistics = mss;
        }

        //normalization
        if(snrImpact==0) snrImpact=1; //avoid NaN
        if(meanSNR==0) meanSNR = 1; //avoid NaN
        if(transceivers==0) transceivers = 1; //avoid NaN
        if(minSNR==0) minSNR = 1; //avoid NaN
        if(spectrumUtilization==0) spectrumUtilization = 1; //avoid NaN
        if(physicalHops==0) physicalHops = 1; //avoid NaN
        if(virtualHops==0) virtualHops = 1; //avoid NaN

        for(MultihopSolution ms: ams) {
            ms.statistics.physicalHops /= physicalHops;
            ms.statistics.virtualHops /= virtualHops;
            ms.statistics.spectrumUtilization /= spectrumUtilization;
            ms.statistics.minSNR /= minSNR;
            ms.statistics.meanSNR /= meanSNR;
            ms.statistics.SNRImpact /= snrImpact;
            ms.statistics.transceivers /= transceivers;
        }


    }

    private void printVirtualRoute(){
        System.out.println("Virtual routes:");
        for(String o : virtualRouting.keySet()){
            for(String d : virtualRouting.get(o).keySet()){
                if(virtualRouting.get(o).get(d).size()<2) continue;
                System.out.print(o + "-" + d + ": " );
                for(MultihopSolution ms : virtualRouting.get(o).get(d)){
                    if(ms.needsComplement) continue;
                    if(ms.virtualRoute.size()>0)
                        System.out.print("  " + ms.toString());
                }
                System.out.println();
            }
        }
        System.out.println();
        System.out.println();
    }

    protected class MultihopSolutionStatistics {

        double physicalHops = 0.0D;
        double virtualHops;
        double spectrumUtilization;
        double minSNR;
        double meanSNR;
        int transceivers;
        double SNRImpact;

        MultihopSolutionStatistics(MultihopSolution sol, RequestForConnection rfc, ControlPlane cp, MSSOptimizator msso) {
            this.virtualHops = (double)sol.virtualRoute.size();
            this.spectrumUtilization = 0.0D;
            this.minSNR = 1.0E8D;
            this.meanSNR = 0.0D;
            this.transceivers = 0;
            this.SNRImpact = 0;

            double snr;
            for(Circuit circuit : sol.virtualRoute) {
                this.physicalHops += (double)circuit.getRoute().getHops();
                circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth());
                this.spectrumUtilization += (double)(circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * circuit.getRoute().getHops());
                snr = circuit.getSNR();
                if (snr < this.minSNR) {
                    this.minSNR = snr;
                }
                this.meanSNR += snr;
                if(oSNRImpactPerCircuit.get(circuit)==null){
                    oSNRImpactPerCircuit.put(circuit,cp.computesImpactOnSNROther(circuit));
                }
                this.SNRImpact += oSNRImpactPerCircuit.get(circuit);
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


        MSSOptimizator(){
            oCanEstabilish = new HashMap<>();
            oPhysicalHops = new HashMap<>();
            oSNR = new HashMap<>();
            oSNRImpact = new HashMap<>();
            oSpectrumUtilization = new HashMap<>();
        }
    }

    protected static class MultihopSolution {
        String src;
        String dst;
        ArrayList<Circuit> virtualRoute = new ArrayList<>();
        boolean needsComplement = false;
        Pair pairComplement = null;
        MultihopSolutionStatistics statistics = null;

        @Override
        public MultihopSolution clone() {
            MultihopSolution n = new MultihopSolution();
            n.src = this.src + "";
            n.dst = this.dst + "";
            n.virtualRoute = (ArrayList<Circuit>)this.virtualRoute.clone();
            n.needsComplement = this.needsComplement;
            n.pairComplement = this.pairComplement;
            return n;
        }

        @Override
        public String toString(){
            String res = "[";
            for(Circuit c : virtualRoute){
                res+="(" + c.getSource().getName() + "," + c.getDestination().getName() + "), ";
            }
            res += "]";
            return res;
        }
    }


}




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
    protected static int microRegionsDeep = 2;
    protected int maxVirtualHops = 3;
    protected HashMap<String, HashMap<String, ArrayList<MultihopSolution>>> virtualRouting;
    protected HashMap<String, ArrayList<String>> microRegions;

    public MultihopGrooming() {
    }

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

    private void addNewCircuitVirtualRouting(Circuit circuit) {
        String dst = circuit.getPair().getDestination().getName();
        String src = circuit.getPair().getSource().getName();
        MultihopGrooming.MultihopSolution al = new MultihopGrooming.MultihopSolution();
        al.virtualRoute.add(circuit);
        al.src = src;
        al.dst = dst;
        this.virtualRouting.get(src).get(dst).add(al);
        ArrayList<MultihopGrooming.MultihopSolution> aalAux = new ArrayList();
        aalAux.add(al);
        Iterator it = this.virtualRouting.get(dst).values().iterator();

        Iterator it2;
        while(it.hasNext()) {
            it2 = ((ArrayList)it.next()).iterator();

            while(it2.hasNext()) {
                MultihopGrooming.MultihopSolution clone = ((MultihopGrooming.MultihopSolution)it2.next()).clone();
                clone.virtualRoute.add(0, circuit);
                clone.src = circuit.getSource().getName();
                if (clone.virtualRoute.size() <= this.maxVirtualHops && !hasVirtualLoop(clone.virtualRoute)) {
                    this.virtualRouting.get(clone.src).get(clone.dst).add(clone);
                    aalAux.add(clone);
                }
            }
        }

        it2 = this.virtualRouting.keySet().iterator();

        while(it2.hasNext()) {
            String sA = (String)it2.next();
            Iterator it3 = this.virtualRouting.get(sA).get(src).iterator();

            while(it3.hasNext()) {
                MultihopGrooming.MultihopSolution c1 = (MultihopGrooming.MultihopSolution)it3.next();
                Iterator it4 = aalAux.iterator();

                while(it4.hasNext()) {
                    MultihopGrooming.MultihopSolution cAux = c1.clone();
                    MultihopGrooming.MultihopSolution c2 = ((MultihopGrooming.MultihopSolution)it4.next()).clone();
                    cAux.virtualRoute.addAll(c2.virtualRoute);
                    cAux.dst = c2.dst;
                    if (cAux.virtualRoute.size() <= this.maxVirtualHops && !hasVirtualLoop(cAux.virtualRoute)) {
                        this.virtualRouting.get(cAux.src).get(cAux.dst).add(cAux);
                    }
                }
            }
        }

    }

    private void removeCircuitVirtualRouting(Circuit circuit) {
        Set<String> srcs = this.virtualRouting.keySet();
        Iterator var3 = srcs.iterator();

        while(var3.hasNext()) {
            String src = (String)var3.next();
            Set<String> dsts = ((HashMap)this.virtualRouting.get(src)).keySet();
            Iterator var6 = dsts.iterator();

            while(var6.hasNext()) {
                String dst = (String)var6.next();
                ArrayList<MultihopGrooming.MultihopSolution> virtRoutes = (ArrayList)((HashMap)this.virtualRouting.get(src)).get(dst);
                ArrayList<MultihopGrooming.MultihopSolution> newVR = new ArrayList();
                Iterator var10 = virtRoutes.iterator();

                while(var10.hasNext()) {
                    MultihopGrooming.MultihopSolution vr = (MultihopGrooming.MultihopSolution)var10.next();
                    if (!vr.virtualRoute.contains(circuit)) {
                        newVR.add(vr);
                    }
                }

                ((HashMap)this.virtualRouting.get(src)).put(dst, newVR);
            }
        }

    }

    private ArrayList<MultihopGrooming.MultihopSolution> allVirtualRouting(RequestForConnection rfc) {
        ArrayList<MultihopGrooming.MultihopSolution> allSolutions = new ArrayList();
        Iterator var3 = this.virtualRouting.get(rfc.getPair().getSource().getName()).get(rfc.getPair().getDestination().getName()).iterator();

        while(var3.hasNext()) {
            MultihopGrooming.MultihopSolution ms = (MultihopGrooming.MultihopSolution)var3.next();
            allSolutions.add(ms.clone());
        }

        var3 = this.microRegions.get(rfc.getPair().getDestination().getName()).iterator();

        while(var3.hasNext()) {//for TGS who needs complement
            String nearNode = (String)var3.next();
            ArrayList<MultihopGrooming.MultihopSolution> multihopSolutions = this.virtualRouting.get(rfc.getPair().getSource().getName()).get(nearNode);
            Iterator var7 = multihopSolutions.iterator();

            while(var7.hasNext()) {
                MultihopGrooming.MultihopSolution ms = (MultihopGrooming.MultihopSolution)var7.next();
                if(ms.virtualRoute.size()<this.maxVirtualHops) {
                    MultihopGrooming.MultihopSolution msAux = ms.clone();
                    msAux.needsComplement = true;
                    msAux.pairComplement = new Pair(((Circuit) msAux.virtualRoute.get(msAux.virtualRoute.size() - 1)).getDestination(), rfc.getPair().getDestination());
                    msAux.dst = rfc.getPair().getDestination().getName();
                    allSolutions.add(msAux);
                }
            }
        }

        MultihopGrooming.MultihopSolution ms = new MultihopGrooming.MultihopSolution();
        ms.src = rfc.getPair().getSource().getName();
        ms.dst = rfc.getPair().getDestination().getName();
        ms.virtualRoute = new ArrayList();
        ms.needsComplement = true;
        ms.pairComplement = rfc.getPair();
        allSolutions.add(ms);
        return allSolutions;
    }

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

    private Circuit complementSolution(MultihopGrooming.MultihopSolution ms, RequestForConnection rfc, ControlPlane cp) throws Exception {
        Node s = ms.pairComplement.getSource();
        Node d = ms.pairComplement.getDestination();
        Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(s, d));
        if (!cp.establishCircuit(newCircuit)) {
            return null;
        } else {
            newCircuit.removeRequest(rfc);
            rfc.setCircuit(new ArrayList());
            return newCircuit;
        }
    }

    private boolean hasSuficientResidualCapacity(MultihopGrooming.MultihopSolution ms, RequestForConnection rfc) {
        boolean hasSuficientResitualCapacity = true;

        Circuit circuit;
        for(Iterator var4 = ms.virtualRoute.iterator(); var4.hasNext(); hasSuficientResitualCapacity = hasSuficientResitualCapacity && circuit.getResidualCapacity() > rfc.getRequiredBandwidth()) {
            circuit = (Circuit)var4.next();
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

        ArrayList<MultihopGrooming.MultihopSolution> multihopSolutions = this.allVirtualRouting(rfc);
        this.avMS(multihopSolutions, rfc, cp);
        Collections.sort(multihopSolutions, new Comparator<MultihopGrooming.MultihopSolution>() {
            public int compare(MultihopGrooming.MultihopSolution o1, MultihopGrooming.MultihopSolution o2) {
                Double c1 = MultihopGrooming.this.costFunction(o1, rfc, cp);
                Double c2 = MultihopGrooming.this.costFunction(o2, rfc, cp);
                int res = c1.compareTo(c2);
                return res;
            }
        });
        Iterator var4 = multihopSolutions.iterator();

        MultihopGrooming.MultihopSolution ms;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            ms = (MultihopGrooming.MultihopSolution)var4.next();
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
        Iterator it;
        MSSOptimizator msso = new MSSOptimizator();
        for(it = ams.iterator(); it.hasNext(); ms.statistics = mss) {
            ms = (MultihopGrooming.MultihopSolution)it.next(); //for each TGS
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
            ms = (MultihopGrooming.MultihopSolution)it.next();
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
        public double SNRImpact;

        public MultihopSolutionStatistics(MultihopGrooming.MultihopSolution sol, RequestForConnection rfc, ControlPlane cp, MSSOptimizator msso) {
            this.virtualHops = (double)sol.virtualRoute.size();
            this.spectrumUtilization = 0.0D;
            this.minSNR = 1.0E8D;
            this.meanSNR = 0.0D;
            this.transceivers = 0;
            this.SNRImpact = 0;

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
        public HashMap<String,Boolean> oCanEstabilish;
        public HashMap<String,Double> oPhysicalHops;
        public HashMap<String,Double> oSNR;
        public HashMap<String,Double> oSNRImpact;
        public HashMap<String,Double> oSpectrumUtilization;

        public HashMap<Circuit,Double> oSNRImpactPerCircuit; //store the snrImpact per circuit.

        public MSSOptimizator(){
            oCanEstabilish = new HashMap<>();
            oPhysicalHops = new HashMap<>();
            oSNR = new HashMap<>();
            oSNRImpact = new HashMap<>();
            oSpectrumUtilization = new HashMap<>();
            oSNRImpactPerCircuit = new HashMap<>();
        }
    }

    protected static class MultihopSolution {
        public String src;
        public String dst;
        public ArrayList<Circuit> virtualRoute = new ArrayList();
        public boolean needsComplement = false;
        public Pair pairComplement = null;
        public MultihopGrooming.MultihopSolutionStatistics statistics = null;

        protected MultihopSolution() {
        }

        public MultihopGrooming.MultihopSolution clone() {
            MultihopGrooming.MultihopSolution n = new MultihopGrooming.MultihopSolution();
            n.src = this.src + "";
            n.dst = this.dst + "";
            n.virtualRoute = (ArrayList)this.virtualRoute.clone();
            n.needsComplement = this.needsComplement;
            return n;
        }
    }
}




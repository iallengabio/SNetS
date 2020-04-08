package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import network.Node;
import network.Pair;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

public class AuxiliaryGraphGrooming implements TrafficGroomingAlgorithmInterface {
    private Double alfa;
    private Double beta;
    private Double gama;
    private Double delta;
    private Double epsilon;
    private Double fi;
    private boolean flagInit = true;
    private HashMap<String, HashSet<Node>> microRegions;
    private static int microRegionsDeep = 2;

    @Override
    public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) throws Exception {
        if(flagInit){
            init(cp);
            flagInit = false;
        }

        AuxiliaryGraph AG = makeAuxiliaryGraph(cp,rfc);
        List<List<AuxiliaryGraph.Edge>> solutions = AG.djk(rfc.getPair().getSource().toString(), rfc.getPair().getDestination().toString());


        Collections.sort(solutions, new Comparator<List<AuxiliaryGraph.Edge>>() {
            @Override
            public int compare(List<AuxiliaryGraph.Edge> o1, List<AuxiliaryGraph.Edge> o2) {
                return auxCost(o1).compareTo(auxCost(o2));
            }

            private Double auxCost(List<AuxiliaryGraph.Edge> le){
                double c = 0;
                for (AuxiliaryGraph.Edge e:le) {
                    c+=e.getCost();
                }
                return c;
            }
        });
        Iterator<List<AuxiliaryGraph.Edge>> it = solutions.iterator();

        while(it.hasNext()){
            List<AuxiliaryGraph.Edge> solution = it.next();
            if(applySolution(solution,rfc,cp)){
                return true;
            }
        }

        return false;
    }

    private boolean applySolution(List<AuxiliaryGraph.Edge> solution, RequestForConnection rfc, ControlPlane cp){
        List<MyEdge> newCircuitEdges = createNewCircuits(solution,rfc,cp);
        if(newCircuitEdges==null) return false;
        if(!expandCircuits(solution,rfc,cp)){
            releaseCircuits(newCircuitEdges,cp);
            return false;
        }

        //resources allocated
        //acomodate rfc
        List<Circuit> lc = new ArrayList<>();
        for(AuxiliaryGraph.Edge e: solution){
            MyEdge me = (MyEdge) e;
            me.getCircuit().getRequests().add(rfc);
            lc.add(me.getCircuit());
        }
        rfc.setCircuit(lc);

        return true;
    }

    private boolean expandCircuits(List<AuxiliaryGraph.Edge> solution, RequestForConnection rfc, ControlPlane cp){
        //for rollback
        List<MyEdge> expandedCircuits = new ArrayList<>();
        List<Integer> upExps = new ArrayList<>();
        List<Integer> downExps = new ArrayList<>();

        for(AuxiliaryGraph.Edge e: solution){
            MyEdge me = (MyEdge) e;
            if(!me.canBeExpanded()){
                retractCircuits(expandedCircuits,upExps,downExps,cp);
                return false;
            }
            Circuit c = me.getCircuit();
            int slotsNeeded = c.getModulation().requiredSlots(c.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (c.getSpectrumAssigned()[1] - c.getSpectrumAssigned()[0] + 1);
            if(slotsNeeded>0){
                List<int[]> composition = IntersectionFreeSpectrum.merge(c.getRoute(), c.getGuardBand());
                int bandFreeAdjInferior = IntersectionFreeSpectrum.freeSlotsDown(c.getSpectrumAssigned(), composition, c.getGuardBand());
                int bandFreeAdjSuperior = IntersectionFreeSpectrum.freeSlotsUpper(c.getSpectrumAssigned(), composition, c.getGuardBand());
                int[] expansion = decideToExpand(slotsNeeded, bandFreeAdjInferior, bandFreeAdjSuperior);
                try {
                    if(cp.expandCircuit(c, expansion[0], expansion[1])){
                        expandedCircuits.add(me);
                        downExps.add(expansion[0]);
                        upExps.add(expansion[1]);
                    }else{
                        retractCircuits(expandedCircuits,upExps,downExps,cp);
                        return false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new UnsupportedOperationException();
                }
            }
        }

        return true;
    }

    private void retractCircuits(List<MyEdge> circuitsToRetract, List<Integer> upExps, List<Integer> downExps, ControlPlane cp){
        int i;
        for(i=0;i<circuitsToRetract.size();i++){
            try {
                cp.retractCircuit(circuitsToRetract.get(i).getCircuit(), downExps.get(i), upExps.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
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

    private List<MyEdge> createNewCircuits(List<AuxiliaryGraph.Edge> solution, RequestForConnection rfc, ControlPlane cp){
        List<MyEdge> newCircuitEdges = new ArrayList<>();
        //create new circuits
        for(AuxiliaryGraph.Edge e: solution){
            MyEdge me = (MyEdge) e;
            if(me.isActive())continue;
            Circuit newCircuit = cp.createNewCircuit(rfc,new Pair(me.getSourceNode(),me.getDestinationNode()));
            boolean canStabilish;
            try {
                canStabilish = cp.establishCircuit(newCircuit);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new UnsupportedOperationException();
            }
            if (!canStabilish) {
                //rollback
                releaseCircuits(newCircuitEdges,cp);
                return null;
            } else {
                newCircuit.removeRequest(rfc);
                rfc.setCircuit(new ArrayList<>());
                me.setCircuit(newCircuit);
                me.setActive(true);
                newCircuitEdges.add(me);
            }

        }
        return newCircuitEdges;
    }

    private void releaseCircuits(List<MyEdge> newCircuitEdges, ControlPlane cp){
        for(MyEdge c: newCircuitEdges){
            try {
                cp.releaseCircuit(c.getCircuit());
                c.setActive(false);
            } catch (Exception e1) {
                e1.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {
        for (Circuit circuit : rfc.getCircuits()) {
            if (circuit.getRequests().size() == 1) {
                cp.releaseCircuit(circuit);
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

    private AuxiliaryGraph makeAuxiliaryGraph(ControlPlane cp,RequestForConnection rfc) {
        AuxiliaryGraph AG = new AuxiliaryGraph(new ArrayList<>(cp.getMesh().getNodeList()));

        //add active circuits
        for (Circuit c:cp.getConnections()) {
            AG.addEdge(new MyEdge(c,rfc));
        }

        //add pontential new circuits
        /*for (Node n1:cp.getMesh().getNodeList()) {
            for (Node n2:cp.getMesh().getNodeList()) {
                if(!n1.equals(n2)){
                    AG.addEdge(new MyEdge(n1,n2,rfc,cp));
                }
            }
        }*/

        AG.addEdge(new MyEdge(rfc.getPair().getSource(),rfc.getPair().getDestination(),rfc,cp));
        if(microRegions==null)initMicroRegions(cp);
        for(Node n : microRegions.get(rfc.getPair().getDestination().getName())){
            AG.addEdge(new MyEdge(n,rfc.getPair().getDestination(),rfc,cp));
        }


        return AG;
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

    private void init(ControlPlane cp){
        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
        if(uv.get("alfa")!=null) this.alfa = Double.parseDouble((String)uv.get("alfa"));
        else this.alfa = 0.0;
        if(uv.get("beta")!=null) this.beta = Double.parseDouble((String)uv.get("beta"));
        else this.beta = 0.0;
        if(uv.get("gama")!=null) this.gama = Double.parseDouble((String)uv.get("gama"));
        else this.gama = 0.0;
        if(uv.get("delta")!=null) this.delta = Double.parseDouble((String)uv.get("delta"));
        else this.delta = 0.0;
        if(uv.get("epsilon")!=null) this.epsilon = Double.parseDouble((String)uv.get("epsilon"));
        else this.epsilon = 0.0;
        if(uv.get("fi")!=null) this.fi = Double.parseDouble((String)uv.get("fi"));
        else this.fi = 0.0;
    }

    public class MyEdge implements AuxiliaryGraph.Edge{
        private Node source;
        private Node destination;
        private Circuit circuit;
        private RequestForConnection rfc;
        private ControlPlane cp;
        private boolean active;

        //statistics
        private double virtualHop=0;
        private double physicallHop=0;
        private double spectrumUtilization=0;
        private double newBVTs=0;
        private Double cost=null;


        public MyEdge(Circuit circuit, RequestForConnection rfc) {
            this.circuit = circuit;
            source = circuit.getSource();
            destination = circuit.getDestination();
            active=true;
            this.rfc = rfc;
        }

        public MyEdge(Node s, Node d,RequestForConnection rfc, ControlPlane cp){
            source = s;
            destination = d;
            circuit = null;
            active = false;
            this.rfc = rfc;
            this.cp = cp;
        }

        @Override
        public double getCost() {
            if(cost!=null) return cost;
            if(active){
                if(canBeExpanded()){
                    analise();
                }else{
                    cost = Double.MAX_VALUE;
                    return cost;
                }

            }else{
                Circuit newCircuit = cp.createNewCircuit(rfc, new Pair(source, destination));
                boolean canStabilish;
                try {
                    canStabilish = cp.establishCircuit(newCircuit);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new UnsupportedOperationException();
                }
                if (!canStabilish) {
                    cost = Double.MAX_VALUE;
                    return cost;
                } else {
                    newCircuit.removeRequest(rfc);
                    this.newBVTs=2;
                    this.circuit = newCircuit;
                    analise();
                    this.circuit = null;
                    try {
                        cp.releaseCircuit(newCircuit);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new UnsupportedOperationException();
                    }
                }

            }
            cost = alfa*virtualHop+beta*physicallHop+gama*spectrumUtilization+delta*newBVTs;
            return cost;
        }

        public boolean canBeExpanded() {
            int[] exp = Grooming.circuitExpansiveness(circuit);
            int circExCap = exp[0] + exp[1];
            int slotsNeeded = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1);
            return circExCap >= slotsNeeded;
        }

        private void analise(){
            this.virtualHop = 1;
            this.physicallHop = circuit.getRoute().getHops();
            this.spectrumUtilization = circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * circuit.getRoute().getHops();

        }

        @Override
        public String getSource() {
            return source.toString();
        }

        public Node getSourceNode() {
            return source;
        }

        @Override
        public String getDestination() {
            return destination.toString();
        }


        public Node getDestinationNode() {
            return destination;
        }

        public boolean isActive() {
            return active;
        }

        public Circuit getCircuit() {
            return circuit;
        }

        public void setCircuit(Circuit circuit) {
            this.circuit = circuit;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

}

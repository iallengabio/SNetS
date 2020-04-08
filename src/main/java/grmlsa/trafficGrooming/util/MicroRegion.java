package grmlsa.trafficGrooming.util;

import network.ControlPlane;
import network.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MicroRegion {

    private HashMap<String, HashSet<Node>> microRegions;
    private int microRegionsDeep;




    public MicroRegion(ControlPlane cp) {
        this.microRegions = new HashMap<>();

        for (Node n : cp.getMesh().getNodeList()) {
            this.microRegions.put(n.getName(), computeMicroRegion(cp, n,microRegionsDeep));
        }

        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();

        if(uv.get("micro_region_deep")!=null) this.microRegionsDeep = Integer.parseInt(uv.get("micro_region_deep"));
        else microRegionsDeep = 2;

    }

    public HashSet<Node> get(String s){
        return microRegions.get(s);
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

}

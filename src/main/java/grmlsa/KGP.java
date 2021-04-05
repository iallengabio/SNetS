package grmlsa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import network.Node;
import simulationControl.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class serves to compute the k shortest paths for all pairs of source (s) and destination (d) nodes 
 * of a given network topology considering the links and a given weight for each link.
 * 
 * @author Iallen
 */
public class KGP implements KRoutingAlgorithmInterface {

	// Used as a separator between the names of nodes
    private static final String DIV = "-";

    // Number of shortest routes to be computed for each pair(s, d)
    private final int k;

    //KGP weights
    private Map<String, Map<String, Double>> kgpWeights;

    // List with the k shortest routes for all pairs (s, d)
    private HashMap<String, List<Route>> routesForAllPairs;

    private final Util util;
    /**
     * Constructor
     *
     * @param cp
     * @param k    int - number of routes to be computed for each pair(s, d)
     */
    public KGP(ControlPlane cp, int k) {
        this.k = k;
        kgpWeights = cp.getMesh().getOthersConfig().getKgpWeights();
        this.computeAllRoutes(cp.getMesh());
        //this.saveKRoutesByPar(cp.getMesh().getNodeList());
        util = cp.getMesh().getUtil();
        //salvekRoutesByPar(mesh.getNodeList());
    }

    /**
     * Compute the k shortest routes for each pair(s, d)
     * 
     * @param mesh Mesh
     */
    private void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<>();
        for (Node n1 : mesh.getNodeList()) {
            for (Node n2 : mesh.getNodeList()) {
                if (n1 == n2)
                    continue;
                if(n1.getName().equals("6")&&n2.getName().equals("14")){
                    int a = 1;
                }
                routesForAllPairs.put(n1.getName() + DIV + n2.getName(), this.computeRoutes(n1, n2, mesh));
            }
        }
    }

    /**
     * Compute the k shortest paths between two nodes
     *
     * @param n1 Node
     * @param n2 Node
     * @param mesh Mesh
     * @return List<Route>
     */
    private List<Route> computeRoutes(Node n1, Node n2, Mesh mesh) {
        Comparator<Route> kgpComparator = new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                int res = getRouteWeight(o1).compareTo(getRouteWeight(o2));
                if(res==0) res = 1;
                return res;
            }
        };
        TreeSet<Route> chosenRoutes = new TreeSet<>(kgpComparator);
        TreeSet<Route> routesUnderConstruction = new TreeSet<>(kgpComparator);

        Route r = new Route();
        r.addNode(n1);
        routesUnderConstruction.add(r);

        double highestAmongShortest = Double.POSITIVE_INFINITY;

        while (!routesUnderConstruction.isEmpty()) {

            Route expand = routesUnderConstruction.pollFirst();

            if (hasLoop(expand)) continue;

            if (expand.getDestination().equals(n2)) { // route completed

                chosenRoutes.add(expand);
                if (chosenRoutes.size() > this.k) { // Already has k chosen routes, should remain only the smallest k
                    Route rl = chosenRoutes.pollLast();
                    highestAmongShortest = getRouteWeight(chosenRoutes.last());
                }
                continue;
            }else {
                if (getRouteWeight(expand) <= highestAmongShortest) { //  Search more routes from this

                    for (Node no : mesh.getAdjacents(expand.getDestination())) {

                        Route rAux = expand.clone();
                        rAux.addNode(no);

                        routesUnderConstruction.add(rAux);
                    }
                }
            }
        }

        return new ArrayList<Route>(chosenRoutes);
    }

    private Double getRouteWeight(Route r){
        double res = r.getDistanceAllLinks();
        for(Link l: r.getLinkList()){
            Map<String, Double> maux = kgpWeights.get(l.getSource().getName());
            if(maux!=null){
                Double kgpweight = maux.get(l.getDestination().getName());
                if(kgpweight!=null) res += kgpweight;
            }
            //res += kgpWeights.get(l.getSource().getName()).get(l.getDestination().getName());
        }
        return res;
    }

    /**
     * Check if a given route has a loop
     * 
     * @param r Route
     * @return boolean
     */
    private boolean hasLoop(Route r) {
        HashSet<String> nos = new HashSet<>();
        for (Node n : r.getNodeList()) {
            if (nos.contains(n.getName()))
            	return true;
            nos.add(n.getName());
        }
        return false;
    }

    /**
     * Returns the k shortest paths between two nodes
     * 
     * @param n1 Node
     * @param n2 Node
     * @return List<Route>
     */
    public List<Route> getRoutes(Node n1, Node n2) {
        return this.routesForAllPairs.get(n1.getName() + DIV + n2.getName());
    }
    
    /**
     * This method saves in files all the routes for all the pairs.
     * 
     * @param nodeList Vector<Node>
     */
    private void saveKRoutesByPar(Vector<Node> nodeList) {
    	List<String> routesList = new ArrayList<String>();
		
		for(int i = 0; i < nodeList.size(); i++){
			Node source = nodeList.get(i);

            for (Node destination : nodeList) {
                if (!source.getName().equals(destination.getName())) {
                    String pair = source.getName() + DIV + destination.getName();

                    List<Route> routes = routesForAllPairs.get(pair);
                    for (Route rAux : routes) {
                        StringBuilder sb = new StringBuilder();
                        for (int n = 0; n < rAux.getNodeList().size(); n++) {
                            sb.append(rAux.getNodeList().get(n).getName());
                            if (n < rAux.getNodeList().size() - 1) {
                                sb.append("-");
                            }
                        }

                        routesList.add(sb.toString());
                    }
                }
            }
		}
		
		Gson gson = new GsonBuilder().create();
        String json = gson.toJson(routesList);
        
        try {
        	String separator = System.getProperty("file.separator");
        	
        	FileWriter fw = new FileWriter("kRoutesByParIT2.txt");
			BufferedWriter out = new BufferedWriter(fw);
            
			out.append(json);
			
			out.close();
			fw.close();
            
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    /**
     * This method saves in files all the routes for all the pairs.
     * 
     * @param nodeList Vector<Node>
     */
    private void saveRoutesByPar(Vector<Node> nodeList) {
        try {
        	
        	FileWriter fw = new FileWriter(util.projectPath + "/routesByPar.txt");
			BufferedWriter out = new BufferedWriter(fw);
        	
			for(int i = 0; i < nodeList.size(); i++){
	    	    Node source = nodeList.get(i);

                for (Node destination : nodeList) {
                    if (!source.getName().equals(destination.getName())) {
                        String pair = source.getName() + "-" + destination.getName();

                        out.append("Pair ").append(pair).append("\n");

                        List<Route> routes = routesForAllPairs.get(pair);
                        for (Route rAux : routes) {
                            StringBuilder sb = new StringBuilder();
                            for (int n = 0; n < rAux.getNodeList().size(); n++) {
                                sb.append(rAux.getNodeList().get(n).getName());
                                if (n < rAux.getNodeList().size() - 1) {
                                    sb.append("-");
                                }
                            }

                            out.append(sb.toString());
                            out.append("\n");
                        }
                    }
                }
	        }
			
			out.close();
			fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
    public HashMap<String, List<Route>> getRoutesForAllPairs() {
		return routesForAllPairs;
	}
}

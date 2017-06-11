package grmlsa;

import network.Mesh;
import network.Node;

import java.util.*;

/**
 * This class serves to compute the k shortest paths for all pairs of source (s) and destination (d) nodes 
 * of a given network topology
 * 
 * @author Iallen
 */
public class NewKShortestPaths {
	
	// Used as a separator between the names of nodes
    private static final String DIV = "-";

    // Number of shortest routes to be computed for each pair(s, d)
    private int k;

    // List with the k shortest routes for all pairs (s, d)
    private HashMap<String, List<Route>> routesForAllPairs;

    /**
     * Constructor
     * 
     * @param mesh Mesha - network topology
     * @param k    int - number of routes to be computed for each pair(s, d)
     */
    public NewKShortestPaths(Mesh mesh, int k) {
        this.k = k;
        this.computeAllRoutes(mesh);
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
        TreeSet<Route> chosenRoutes = new TreeSet<>();
        TreeSet<Route> routesUnderConstruction = new TreeSet<>();

        Route r = new Route();
        r.addNode(n1);
        routesUnderConstruction.add(r);

        Double highestAmongShortest = 999999999999999.9;

        while (!routesUnderConstruction.isEmpty()) {

            Route expand = routesUnderConstruction.pollFirst();

            if (hasLoop(expand)) continue;

            if (expand.getDestination().equals(n2)) { // route completed

                if (chosenRoutes.size() < this.k) {
                    chosenRoutes.add(expand);
                    
                } else { // Already has k chosen routes, should remain only the smallest k
                    chosenRoutes.add(expand);

                    highestAmongShortest = chosenRoutes.last().getDistanceAllLinks();
                }
                continue;
            }

            if (expand.getDistanceAllLinks() > highestAmongShortest) {// It's no use keeping looking on this route

            } else { //  Search more routes from this

                for (Node no : mesh.getAdjacents(expand.getDestination())) {

                    Route rAux = expand.clone();
                    rAux.addNode(no);

                    routesUnderConstruction.add(rAux);
                }
            }
        }

        return new ArrayList<Route>(chosenRoutes);
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

}

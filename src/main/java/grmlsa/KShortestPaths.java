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
public class KShortestPaths {
	
	// Used as a separator between the names of nodes
    private static final String DIV = "-";

    // Number of shortest routes to be computed for each pair(s, d)
    private int k;

    // List with the k shortest routes for all pairs (s, d)
    private HashMap<String, List<Route>> routesForAllPairs;

    /**
     * Creates a new instance of KShortestPaths
     * 
     * @param mesh Mesha - network topology
     * @param k    int - number of routes to be computed for each pair(s, d)
     */
    public KShortestPaths(Mesh mesh, int k) {
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

                routesForAllPairs.put(n1.getName() + DIV + n2.getName(),
                        this.computeRoutes(n1, n2, mesh));
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
        TreeSet<Route> res = new TreeSet<>();

        List<Vector<Node>> routesUnderConstruction = new ArrayList<>();

        Vector<Node> ini = new Vector<>();
        ini.add(n1);

        routesUnderConstruction.add(ini);

        while (!routesUnderConstruction.isEmpty()) {
        	//System.out.println("\n\n routes under construction");
            for (Vector<Node> vector : routesUnderConstruction) {
                String aux = "";
                for (Node node : vector) {
                    aux = aux + node.getName() + "->";
                }
                //System.out.println(aux);
            }


            Vector<Node> expand = routesUnderConstruction.remove(0);

            if (expand.get(expand.size() - 1).equals(n2)) {// route found
                Route r = new Route(expand);


                res.add(r);
                continue;
            }

            for (Node node : mesh.getAdjacents(expand.get(expand.size() - 1))) {
                if (expand.contains(node))
                    continue; // Do not create routes with loops
                Vector<Node> aux = (Vector<Node>) expand.clone();
                aux.add(node);
                routesUnderConstruction.add(aux);
            }
        }

        if (res.size() > k)
            return new ArrayList<>(res).subList(0, k);
        else
            return new ArrayList<>(res);
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

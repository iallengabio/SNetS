package grmlsa;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import network.Mesh;
import network.Node;
import simulationControl.Util;

public class KFixedRoutes implements KRoutingAlgorithmInterface {

    private static final String DIV = "-";

    private ArrayList<String> routeList;

    // List with the k shortest routes for all pairs (s, d)
    private HashMap<String, List<Route>> routesForAllPairs;
    
    // Number of shortest routes to be computed for each pair(s, d)
    private int k;
    
    /**
     * Creates a new instance of KFixedRoutes
     */
    public KFixedRoutes(Mesh mesh) {
        try {
        	String separator = System.getProperty("file.separator");
        	String filePath = mesh.getUtil().projectPath + separator + "kRoutesByPar.txt";
        	String routesListGson = "";
        	
        	Scanner scanner = new Scanner(new File(filePath));
        	while (scanner.hasNext()) {
        		routesListGson += scanner.next();
            }
        	
        	Gson gson = new GsonBuilder().create();
        	Type typeTemp = new TypeToken<ArrayList<String>>(){}.getType();
        	
        	List<String> routeListTemp = gson.fromJson(routesListGson, typeTemp);
        	routeList = (ArrayList<String>)routeListTemp;
        	
        	scanner.close();
        	
        } catch (Exception e) {
            System.err.println("The file kRoutesByPar.txt was not found!");

            e.printStackTrace();
        }
        
        computeAllRoutes(mesh);
    }

    /**
     * Computes the smallest paths for each pair
     *
     * @param mesh Mesh
     */
    private void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<>();
        
        for (int i = 0; i < routeList.size(); i++) {
        	String nodes[] = routeList.get(i).split("-");
            Vector<Node> route = new Vector<Node>();
            
            for(int n = 0; n < nodes.length; n++){
            	route.add(mesh.searchNode(nodes[n]));
            }
            
            String pair = nodes[0] + DIV + nodes[nodes.length - 1];
            
            List<Route> routes = routesForAllPairs.get(pair);
            if(routes == null){
            	routes = new ArrayList<>();
            	routesForAllPairs.put(pair, routes);
            }
            
            routes.add(new Route(route));
            
            if(routes.size() > k){
            	k = routes.size();
            }
        }
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
     * Returns the k shortest paths by pair
     * 
     * @param pair String
     * @return List<Route>
     */
    public List<Route> getRoutes(String pair) {
        return this.routesForAllPairs.get(pair);
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

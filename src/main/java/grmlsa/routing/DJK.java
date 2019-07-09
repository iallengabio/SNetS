package grmlsa.routing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;
import network.Node;
import simulationControl.Util;

/**
 * This class represents the Dijkstra (Shortest Path) Routing Algorithm.
 * 
 * @author Iallen
 */
public class DJK implements RoutingAlgorithmInterface {

    private static final String DIV = "-";

    private HashMap<String, Route> routesForAllPairs;

    private Util util;

    @Override
    public boolean findRoute(Circuit circuit, Mesh mesh) {
        if (routesForAllPairs == null) {
        	computeAllRoutes(mesh);
        	//salveRoutesByPar(mesh.getNodeList());
            util = mesh.getUtil();
        }

        Node source = circuit.getSource();
        Node destination = circuit.getDestination();

        Route route = routesForAllPairs.get(source.getName() + DIV + destination.getName());

        if (route != null) {
            circuit.setRoute(route);
            return true;
        }

        return false;
    }

    /**
     * Computes the smallest paths for each pair
     *
     * @param mesh Mesh
     */
    private void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<String, Route>();
        for (Node n1 : mesh.getNodeList()) {
            shortestPaths(n1, mesh);
        }
    }

    /**
     * Calculates the smallest route between a source node and all other nodes in the network
     * Dijkstra's algorithm
     *
     * @param source Node
     * @param mesh  Mesh
     */
    private void shortestPaths(Node source, Mesh mesh) {
        HashMap<Node, Double> undefined = new HashMap<>(); //Current distances from the nodes to the origin
        HashMap<Node, Vector<Node>> routes = new HashMap<>(); //Current routes from source to each node

        for (Node n : mesh.getNodeList()) { //Signaling infinite distance to all nodes in the network
            undefined.put(n, 999999999999999.0);
        }

        undefined.put(source, 0.0); //Distance 0 from the source node to itself
        Node nAux1;
        Vector<Node> rAux;

        nAux1 = source;
        rAux = new Vector<>();
        rAux.add(nAux1);
        routes.put(nAux1, rAux);

        while (!undefined.isEmpty()) {
            nAux1 = minDistAt(undefined);

            //Open the vertex
            for (Node n : mesh.getAdjacents(nAux1)) {
                if (!undefined.containsKey(n)) continue;

                rAux = (Vector<Node>) routes.get(nAux1).clone();
                rAux.add(n);
                
                //Check if it is necessary to update the route
                if (undefined.get(n) == null || undefined.get(n) > undefined.get(nAux1) + mesh.getLink(nAux1.getName(), n.getName()).getDistance()) {
                    undefined.put(n, undefined.get(nAux1) + mesh.getLink(nAux1.getName(), n.getName()).getDistance());
                    routes.put(n, rAux);
                }
            }
            Double removed = undefined.remove(nAux1); //Closing the vertex

            routesForAllPairs.put(source.getName() + DIV + nAux1.getName(), new Route(routes.get(nAux1)));

            //System.out.println("closed: " + nAux1.getName() + "    size: " + undefined.keySet().size() + "   removed: " + removed);
        }
    }

    /**
     * Selects the node with the lowest current distance
     *
     * @param undefined HashMap<Node, Double>
     * @return Node
     */
    private Node minDistAt(HashMap<Node, Double> undefined) {

        Iterator<Node> it = undefined.keySet().iterator();

        Node res = it.next();
        Node aux;
        while (it.hasNext()) {
            aux = it.next();

            if (undefined.get(res) > undefined.get(aux)) res = aux;
        }

        return res;
    }
    
    /**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
	public HashMap<String, Route> getRoutesForAllPairs() {
		return routesForAllPairs;
	}
    
    /**
     * This method saves in files the routes for all the pairs.
     * 
     * @param nodeList Vector<Node>
     */
    private void salveRoutesByPar(Vector<Node> nodeList) {
    	List<String> routesList = new ArrayList<String>();
    	
    	int numNodes = nodeList.size();
        for(int i = 1; i <= numNodes; i++){
    	    for(int j = 1; j <= numNodes; j++){
    		    
    		    if(i != j){
	      		    String pair = i + DIV + j;
	      		    
		        	Route rota = routesForAllPairs.get(pair);
		        	StringBuilder sb = new StringBuilder();
		        	for(int n = 0; n < rota.getNodeList().size(); n++){
		        		sb.append(rota.getNodeList().get(n).getName());
		        		if(n < rota.getNodeList().size() - 1){
		        			sb.append("-");
		        		}
		        	}
		        	
		        	routesList.add(sb.toString());
    		    }
    	    }
        }
        
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(routesList);
        
        try {
        	String separator = System.getProperty("file.separator");
        	
        	FileWriter fw = new FileWriter(util.projectPath + separator + "routesByPar.txt");
			BufferedWriter out = new BufferedWriter(fw);
            
			out.append(json);
			
			out.close();
			fw.close();
            
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

}

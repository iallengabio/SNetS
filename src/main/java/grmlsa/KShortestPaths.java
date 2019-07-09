package grmlsa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import network.Mesh;
import network.Node;
import simulationControl.Util;

/**
 * This class serves to compute the k shortest paths for all pairs of source (s) and destination (d) nodes 
 * of a given network topology
 * 
 * @author Iallen
 */
public class KShortestPaths implements KRoutingAlgorithmInterface {
	
	// Used as a separator between the names of nodes
    private static final String DIV = "-";

    // Number of shortest routes to be computed for each pair(s, d)
    private int k;

    // List with the k shortest routes for all pairs (s, d)
    private HashMap<String, List<Route>> routesForAllPairs;

    private Util util;

    /**
     * Creates a new instance of KShortestPaths
     * 
     * @param mesh Mesha - network topology
     * @param k    int - number of routes to be computed for each pair(s, d)
     */
    public KShortestPaths(Mesh mesh, int k) {
        this.k = k;
        this.computeAllRoutes(mesh);
        salveKRoutesByPar(mesh.getNodeList());
        this.util = mesh.getUtil();
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
                @SuppressWarnings("unchecked")
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

    /**
     * This method saves in files all the routes for all the pairs.
     * 
     * @param nodeList Vector<Node>
     */
    private void salveKRoutesByPar(Vector<Node> nodeList) {
    	List<String> routesList = new ArrayList<String>();
		
		for(int i = 0; i < nodeList.size(); i++){
			Node source = nodeList.get(i);
			
			for(int j = 0; j < nodeList.size(); j++){
				Node destination = nodeList.get(j);
				
				if(!source.getName().equals(destination.getName())){
					String pair = source.getName() + DIV + destination.getName();
					
					List<Route> routes = routesForAllPairs.get(pair);
					for(int r = 0; r < routes.size(); r++){
						Route rAux = routes.get(r);
					  
						StringBuilder sb = new StringBuilder();
						for (int n = 0; n < rAux.getNodeList().size(); n++) {
							sb.append(rAux.getNodeList().get(n).getName());
							if(n < rAux.getNodeList().size() - 1){
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
        	
        	FileWriter fw = new FileWriter(util.projectPath + separator + "kRoutesByPar.txt");
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
    private void salveRoutesByPar(Vector<Node> nodeList) {
        try {
        	
        	FileWriter fw = new FileWriter(util.projectPath + "/routesByPar.txt");
			BufferedWriter out = new BufferedWriter(fw);
        	
			for(int i = 0; i < nodeList.size(); i++){
	    	    Node source = nodeList.get(i);
	    	    
	    	    for(int j = 0; j < nodeList.size(); j++){
	    		    Node destination = nodeList.get(j);
	    		    
	    		    if(!source.getName().equals(destination.getName())){
		      		    String pair = source.getName() + "-" + destination.getName();
		      		  
		      		    out.append("Pair " + pair + "\n");
		      		  
		      		    List<Route> routes = routesForAllPairs.get(pair);
		      		    for(int r = 0; r < routes.size(); r++){
		      			    Route rAux = routes.get(r);
		      			  
		      			    StringBuilder sb = new StringBuilder();
		      			    for (int n = 0; n < rAux.getNodeList().size(); n++) {
		      			        sb.append(rAux.getNodeList().get(n).getName());
			      			    if(n < rAux.getNodeList().size() - 1){
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

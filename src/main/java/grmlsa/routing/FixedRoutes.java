package grmlsa.routing;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;
import network.Node;
import simulationControl.Util;

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

/**
 * This class allow to configure a set of pre-established routes for each pair of origin and destination.
 * 
 * @author Iallen
 */
public class FixedRoutes implements RoutingAlgorithmInterface {

    private static final String DIV = "-";

    private ArrayList<String> routeList;

    private HashMap<String, Route> routesForAllPairs;


    @Override
    public boolean findRoute(Circuit request, Mesh mesh) {
        if (routesForAllPairs == null) {
        	computeAllRoutes(mesh);
        }

        Node source = request.getSource();
        Node destination = request.getDestination();

        Route route = routesForAllPairs.get(source.getName() + DIV + destination.getName());

        if (route != null) {
            request.setRoute(route);
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
        try {
            String separator = System.getProperty("file.separator");
            String filePath = mesh.getUtil().projectPath + separator + "routesByPar.txt";
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
            System.err.println("The file routesByPar.txt was not found!");

            e.printStackTrace();
        }

        routesForAllPairs = new HashMap<String, Route>();
        
        for (int i = 0; i < routeList.size(); i++) {
        	String nodes[] = routeList.get(i).split("-");
            Vector<Node> route = new Vector<Node>();
            
            for(int n = 0; n < nodes.length; n++){
            	route.add(mesh.searchNode(nodes[n]));
            }
            
            String pair = nodes[0] + DIV + nodes[nodes.length - 1];
            routesForAllPairs.put(pair, new Route(route));
        }
    }

    /**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
    public HashMap<String, Route> getRoutesForAllPairs() {
		return routesForAllPairs;
	}
}

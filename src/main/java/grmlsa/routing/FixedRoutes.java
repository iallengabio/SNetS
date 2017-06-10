package grmlsa.routing;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;
import network.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * This class allow to configure a set of pre-established routes for each pair of origin and destination.
 */
public class FixedRoutes implements RoutingInterface {

    private static final String DIV = "-";

    private HashMap<String, List<String>> routesIndex;

    private HashMap<String, Route> routesForAllPairs;

    public FixedRoutes() {
        throw new UnsupportedOperationException();
        /*try {
            routesIndex = new HashMap<String, List<String>>();
            //List<List<String>> lRoutes = FixedRoutesFileReader.readRoutes(Util.projectPath + "/fixedRoutes");
            List<List<String>> lRoutes = null;

            for (List<String> route : lRoutes) {
                routesIndex.put(route.get(0) + DIV + route.get(route.size() - 1), route);
            }

        } catch (Exception e) {
            System.out.println("não foi possível ler o arquivo com as rotas fixas!");

            e.printStackTrace();
        }*/
    }

    @Override
    public boolean findRoute(Circuit request, Mesh mesh) {
        if (routesForAllPairs == null) computeAllRoutes(mesh);

        Node source = request.getSource();
        Node destination = request.getDestination();

        Route r = routesForAllPairs.get(source.getName() + DIV + destination.getName());

        if (r != null) {
            request.setRoute(r);
            return true;
        }

        return false;
    }


    /**
     * computa os menores caminhos para cada par
     *
     * @param mesh
     */
    private void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<String, Route>();
        HashMap<String, Node> nos = new HashMap<String, Node>();

        for (Node no : mesh.getNodeList()) {
            nos.put(no.getName(), no);
        }


        for (String key : routesIndex.keySet()) {
            Vector<Node> r = new Vector<Node>();
            for (String index : routesIndex.get(key)) {
                r.add(nos.get(index));
            }
            routesForAllPairs.put(key, new Route(r));
        }

    }


    /**
     * Retorna as rotas para cada par(o,d) na rede
     * método utilizado apenas para roteamento fixo
     *
     * @return
     */
    private Vector<Route> getRoutesForAllPairs() {
        return new Vector<>(routesForAllPairs.values());
    }

}

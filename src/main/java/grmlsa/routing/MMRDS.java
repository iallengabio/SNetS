package grmlsa.routing;

import grmlsa.Route;
import network.Circuit;
import network.Link;
import network.Mesh;
import network.Node;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents the MMRDS Routing Algorithm.
 * This algorithm is presented in http://sbrc2013.unb.br/files/anais/trilha-principal/artigos/artigo-9.pdf
 * 
 * @author Iallen
 */
public class MMRDS implements RoutingAlgorithmInterface, Serializable {
    private static final String DIV = "-";

    private HashMap<String, Route> routesForAllPairs;
    private Double alpha = 1.0;

    @Override
    public boolean findRoute(Circuit request, Mesh mesh) {
        if (routesForAllPairs == null) {
            computeAllRoutes(mesh);
            //salveRoutesByPar();
        }

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
     * Initializes the cost of all network links with the value one.
     * 
     * @param mesh
     */
    public void inicializeLinkCost(Mesh mesh) {
        Vector<Link> linkList = mesh.getLinkList();
        for (int i = 0; i < linkList.size(); i++) {
            linkList.get(i).setCost(1.0);
        }
    }

    /**
     * Computes the smallest paths for each pair
     * 
     * @param mesh Mesh
     */
    public void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<String, Route>();
        HashMap<String, List<Route>> routesForAllPairs2 = new HashMap<String, List<Route>>();
        HashMap<String, Double> similarityForAllPairs2 = new HashMap<String, Double>();

        inicializeLinkCost(mesh);

        for (Node n1 : mesh.getNodeList()) {
            for (Node n2 : mesh.getNodeList()) {
                if (n1 == n2)
                    continue;
                List<Route> listRoutes = computeRoutes(n1, n2, mesh);
                routesForAllPairs2.put(n1.getName() + DIV + n2.getName(), listRoutes);
                similarityForAllPairs2.put(n1.getName() + DIV + n2.getName(), similarity(listRoutes));
            }
        }

        chooseRoutes(routesForAllPairs2, similarityForAllPairs2, mesh);
    }

    /**
     * Computes the smallest paths for a given pair
     * 
     * @param n1 Node
     * @param n2 Node
     * @param mesh Mesh
     * @return List<Route>
     */
    private List<Route> computeRoutes(Node n1, Node n2, Mesh mesh) {
        TreeSet<Route> chosenRoutes = new TreeSet<>();
        TreeSet<Route> routesUnderConstruction = new TreeSet<>();

        Double sizeRoute = 999999999999999.9;
        Route r = new Route();
        r.addNode(n1);
        routesUnderConstruction.add(r);

        while (!routesUnderConstruction.isEmpty()) {
            Route expand = routesUnderConstruction.pollFirst();

            if (hasLoop(expand))
                continue;

            if (expand.getDestination().equals(n2)) { //Route completed

                if (expand.getDistanceAllLinks() < sizeRoute) { //Check if you found a route smaller than the others already found
                    sizeRoute = expand.getDistanceAllLinks();
                    chosenRoutes = new TreeSet<>(); //Clears the list of routes already found
                }

                chosenRoutes.add(expand);
                continue;
            }

            if (expand.getDistanceAllLinks() >= sizeRoute) {//It's no use keeping looking on this route
                continue;
            }

            //Search more routes from this
            for (Node no : mesh.getAdjacents(expand.getDestination())) {
                Route rAux = expand.clone();
                rAux.addNode(no);
                routesUnderConstruction.add(rAux);
            }
        }

        return new ArrayList<Route>(chosenRoutes);
    }

    /**
     * Checks whether the specified route has a loop
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
     * Chooses the best route by similarity for all pairs
     * 
     * @param routesForAllPairs2 HashMap<String, List<Route>>
     * @param similarityForAllPairs2 HashMap<String, Double>
     * @param mesh Mesh
     */
    public void chooseRoutes(HashMap<String, List<Route>> routesForAllPairs2, HashMap<String, Double> similarityForAllPairs2, Mesh mesh) {
        Set<String> pairs = routesForAllPairs2.keySet();
        ArrayList<String> pairsList = new ArrayList<String>();
        for (String pairName : pairs) {
            pairsList.add(new String(pairName));
        }

        while (!pairsList.isEmpty()) {
            String maxPairName = pairsList.get(0);
            Double maxSimilaridade = similarityForAllPairs2.get(maxPairName);

            for (int i = 1; i < pairsList.size(); i++) {
                String pairName = pairsList.get(i);
                Double similaridade = similarityForAllPairs2.get(pairName);
                
                if (maxSimilaridade < similaridade) {
                    maxSimilaridade = similaridade;
                    maxPairName = pairName;
                }
            }

            chooseLessCostlyRoute(maxPairName, routesForAllPairs2);
            pairsList.remove(maxPairName);
        }
    }

    /**
     * Chooses the best route by similarity for a given pair
     * 
     * @param parName String
     * @param routesForAllPairs2 HashMap<String, List<Route>>
     */
    private void chooseLessCostlyRoute(String parName, HashMap<String, List<Route>> routesForAllPairs2) {
        Double lowerCost = 999999999999999.0;
        Route chosenRoute = null;

        List<Route> pairRoutes = routesForAllPairs2.get(parName);
        for (Route raux : pairRoutes) {
            Double rcost = calculeRouteCust(raux);
            if (rcost < lowerCost) {
                lowerCost = rcost;
                chosenRoute = raux;
            }
        }

        routesForAllPairs.put(parName, chosenRoute);
        updateRouteLinkCosts(chosenRoute);
    }

    /**
     * Updates the weight of the links of a given route
     * 
     * @param route Route
     */
    private void updateRouteLinkCosts(Route route) {
        Vector<Link> linkList = route.getLinkList();
        for (Link laux : linkList) {
            Double cost = laux.getCost();
            laux.setCost(cost + alpha);
        }
    }

    /**
     * This method calculates the cost of a given route
     * 
     * @param route Route
     * @return Double
     */
    private Double calculeRouteCust(Route route) {
        Double res = 0.0;
        Vector<Link> linkList = route.getLinkList();
        for (Link laux : linkList) {
            res = res + laux.getCost();
        }
        return res;
    }

    /**
     * This method calculates the similarity between two routes
     * 
     * @param rm1 Route
     * @param rm2 Route
     * @return Double
     */
    private Double similarity(Route rm1, Route rm2) {
        Route r1, r2;
        Double sim = 0.0;

        if (rm1.getLinkList().size() <= rm2.getLinkList().size()) {
            r1 = rm1;
            r2 = rm2;
        } else {
            r1 = rm2;
            r2 = rm1;
        }

        for (Link laux : r1.getLinkList()) {
            if (r2.containThisLink(laux))
                sim = sim + 1;
        }

        return sim / (rm2.getLinkList().size());
    }

    /**
     * Computes the similarity for a list of routes and checks with a given value
     * 
     * @param lrm List<Route>
     * @param re int
     * @return Double
     */
    private Double similarity(List<Route> lrm, int re) {
        Double sim = 0.0;
        Route ra = lrm.get(re);

        for (int i = 0; i < lrm.size(); i++) {
            if (i == re)
                continue;
            sim = sim + similarity(lrm.get(i), ra);
        }

        return sim / (lrm.size() - 1);
    }

    /**
     * Computes the similarity for a list of routes
     * 
     * @param lrm List<Route>
     * @return Double
     */
    public Double similarity(List<Route> lrm) {
        Double sim = 0.0;

        if (lrm.size() == 1)
            return 1.0;

        for (int i = 0; i < lrm.size(); i++) {
            sim = sim + similarity(lrm, i);
        }

        return sim / lrm.size();
    }

    /**
     * This method sort a give array of pairs by required slots
     * 
     * @param array ArrayList<Circuit>
     */
    public static void sortArrayOfPairsBySlots(ArrayList<Circuit> array) {
        Collections.sort(array, new Comparator<Circuit>() {
            @Override
            public int compare(Circuit r1, Circuit r2) {
                Integer r1NumSlots = r1.getModulation().requiredSlots(r1.getRequiredBandwidth());
                Integer r2NumSlots = r2.getModulation().requiredSlots(r2.getRequiredBandwidth());
                return r1NumSlots.compareTo(r2NumSlots);
            }
        });
    }
}

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
 */
public class MMRDS implements RoutingInterface, Serializable {
    private static final String DIV = "-";

    private HashMap<String, Route> routesForAllPairs;
    private Double alfa = 1.0;

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

    public void inicializeLinkCost(Mesh mesh) {
        Vector<Link> linkList = mesh.getLinkList();
        for (int i = 0; i < linkList.size(); i++) {
            linkList.get(i).setCost(1.0);
        }
    }

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
                similarityForAllPairs2.put(n1.getName() + DIV + n2.getName(), similaridade(listRoutes));
            }
        }

        escolherRotas(routesForAllPairs2, similarityForAllPairs2, mesh);
    }

    private List<Route> computeRoutes(Node n1, Node n2, Mesh mesh) {
        TreeSet<Route> rotasEscolhidas = new TreeSet<>();
        TreeSet<Route> rotasEmContrucao = new TreeSet<>();

        Double tamRoute = 999999999999999.9;
        Route r = new Route();
        r.addNode(n1);
        rotasEmContrucao.add(r);

        while (!rotasEmContrucao.isEmpty()) {
            Route expand = rotasEmContrucao.pollFirst();

            if (possuiLoop(expand))
                continue;

            if (expand.getDestination().equals(n2)) { //rota finalizada

                if (expand.getDistanceAllLinks() < tamRoute) { //verifica se encontrou uma rota menor do que as outras ja encontradas
                    tamRoute = expand.getDistanceAllLinks();
                    rotasEscolhidas = new TreeSet<>(); //limpa a lista das rotas ja encontradas
                }

                rotasEscolhidas.add(expand);
                continue;
            }

            if (expand.getDistanceAllLinks() >= tamRoute) {//nao adianta continuar procurando nesta rota
                continue;
            }

            //procurar mais rotas a partir desta
            for (Node no : mesh.getAdjacents(expand.getDestination())) {
                Route rAux = expand.clone();
                rAux.addNode(no);
                rotasEmContrucao.add(rAux);
            }
        }

        return new ArrayList<Route>(rotasEscolhidas);
    }

    private boolean possuiLoop(Route r) {
        HashSet<String> nos = new HashSet<>();
        for (Node n : r.getNodeList()) {
            if (nos.contains(n.getName()))
                return true;
            nos.add(n.getName());
        }
        return false;
    }

    public void escolherRotas(HashMap<String, List<Route>> routesForAllPairs2, HashMap<String, Double> similarityForAllPairs2, Mesh mesh) {
        Set<String> pares = routesForAllPairs2.keySet();
        ArrayList<String> listPares = new ArrayList<String>();
        for (String parName : pares) {
            listPares.add(new String(parName));
        }

        while (!listPares.isEmpty()) {
            String maxParName = listPares.get(0);
            Double maxSimilaridade = similarityForAllPairs2.get(maxParName);

            for (int i = 1; i < listPares.size(); i++) {
                String parName = listPares.get(i);
                Double similaridade = similarityForAllPairs2.get(parName);
                if (maxSimilaridade < similaridade) {
                    maxSimilaridade = similaridade;
                    maxParName = parName;
                }
            }

            escolheRotaMenosCustosa(maxParName, routesForAllPairs2);
            listPares.remove(maxParName);
        }
    }

    private void escolheRotaMenosCustosa(String parName, HashMap<String, List<Route>> routesForAllPairs2) {
        Double menorCusto = 999999999999999.0;
        Route rescolhida = null;

        List<Route> rotasPar = routesForAllPairs2.get(parName);
        for (Route raux : rotasPar) {
            Double rcost = calcularCustoRota(raux);
            if (rcost < menorCusto) {
                menorCusto = rcost;
                rescolhida = raux;
            }
        }

        routesForAllPairs.put(parName, rescolhida);
        pesarEnlacesRota(rescolhida);
    }

    private void pesarEnlacesRota(Route route) {
        Vector<Link> linkList = route.getLinkList();
        for (Link laux : linkList) {
            Double cost = laux.getCost();
            laux.setCost(cost + alfa);
        }
    }

    private Double calcularCustoRota(Route route) {
        Double res = 0.0;
        Vector<Link> linkList = route.getLinkList();
        for (Link laux : linkList) {
            res = res + laux.getCost();
        }
        return res;
    }

    private Double similaridade(Route rm1, Route rm2) {
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

    private Double similaridade(List<Route> lrm, int re) {
        Double sim = 0.0;
        Route ra = lrm.get(re);

        for (int i = 0; i < lrm.size(); i++) {
            if (i == re)
                continue;
            sim = sim + similaridade(lrm.get(i), ra);
        }

        return sim / (lrm.size() - 1);
    }

    public Double similaridade(List<Route> lrm) {
        Double sim = 0.0;

        if (lrm.size() == 1)
            return 1.0;

        for (int i = 0; i < lrm.size(); i++) {
            sim = sim + similaridade(lrm, i);
        }

        return sim / lrm.size();
    }

    public static void orderArrayOfParesPorSimilaridade(ArrayList<Circuit> array) {
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

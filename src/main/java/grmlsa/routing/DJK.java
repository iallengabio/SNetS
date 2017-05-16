package grmlsa.routing;

import grmlsa.Route;
import network.Circuit;
import network.Mesh;
import network.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class DJK implements RoutingInterface {

    private static final String DIV = "-";

    private HashMap<String, Route> routesForAllPairs;


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
    public void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<String, Route>();
        for (Node n1 : mesh.getNodeList()) {
            shortestPaths(n1, mesh);
        }

    }

    /**
     * calcula a menor rota entre um nó de origem e todos os outros nós da rede, algoritmo de Dijkstra
     *
     * @param mesh
     * @return
     */
    private void shortestPaths(Node source, Mesh mesh) {
        HashMap<Node, Double> undefined = new HashMap<>(); //distâncias atuais dos nós até a origem
        HashMap<Node, Vector<Node>> routes = new HashMap<>(); //rotas atuais da origem até cada nó

        for (Node n : mesh.getNodeList()) { //sinalizando distância infinita para todos os nós da rede
            undefined.put(n, 999999999999999.0);
        }

        undefined.put(source, 0.0); //distancia 0 do nó de origem para ele mesmo
        Node nAux1;
        Vector<Node> rAux;

        nAux1 = source;
        rAux = new Vector<>();
        rAux.add(nAux1);
        routes.put(nAux1, rAux);


        while (!undefined.isEmpty()) {
            nAux1 = minDistAt(undefined);

            //abrir a vértice
            for (Node n : mesh.getAdjacents(nAux1)) {
                if (!undefined.containsKey(n)) continue;

                rAux = (Vector<Node>) routes.get(nAux1).clone();
                rAux.add(n);
                //verificar se é necessário atualizar a rota
                if (undefined.get(n) == null || undefined.get(n) > undefined.get(nAux1) + mesh.getLink(nAux1.getName(), n.getName()).getDistance()) {
                    undefined.put(n, undefined.get(nAux1) + mesh.getLink(nAux1.getName(), n.getName()).getDistance());
                    routes.put(n, rAux);
                }
            }
            Double removed = undefined.remove(nAux1); //fechando o vértice

            routesForAllPairs.put(source.getName() + DIV + nAux1.getName(), new Route(routes.get(nAux1)));

            //System.out.println("closed: " + nAux1.getName() + "    size: " + undefined.keySet().size() + "   removed: " + removed);
        }


    }


    /**
     * seleciona o nó com menor distância atual
     *
     * @param undefined
     * @return
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
     * Retorna as rotas para cada par(o,d) na rede
     * método utilizado apenas para roteamento fixo
     *
     * @return
     */
    public Vector<Route> getRoutesForAllPairs() {
        return new Vector<>(routesForAllPairs.values());
    }

}

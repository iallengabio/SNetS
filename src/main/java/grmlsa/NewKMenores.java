package grmlsa;

import network.Mesh;
import network.Node;

import java.util.*;

public class NewKMenores {
    private static final String DIV = "-";

    private int k;


    private HashMap<String, List<Route>> routesForAllPairs;

    /**
     * @param mesh topologia de rede
     * @param k    quantidade de rotas a serem computadas para cada par(o,d)
     */
    public NewKMenores(Mesh mesh, int k) {
        this.k = k;


        this.computeAllRoutes(mesh);

    }

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
     * calcula as k menores rotas entre dois nós
     *
     * @param n1
     * @param n2
     * @param mesh
     * @return
     */
    private List<Route> computeRoutes(Node n1, Node n2, Mesh mesh) {
        TreeSet<Route> rotasEscolhidas = new TreeSet<>();
        TreeSet<Route> rotasEmContrucao = new TreeSet<>();

        Route r = new Route();
        r.addNode(n1);
        rotasEmContrucao.add(r);

        Double maiorEntreAsMenores = 999999999999999.9;

        while (!rotasEmContrucao.isEmpty()) {

            Route expand = rotasEmContrucao.pollFirst();

            if (possuiLoop(expand)) continue;

            if (expand.getDestino().equals(n2)) { //rota finalizada

                if (rotasEscolhidas.size() < this.k) {
                    rotasEscolhidas.add(expand);
                } else { //já possui k rotas escolhidas, deverão permanecer apenas as k menores
                    rotasEscolhidas.add(expand);

                    maiorEntreAsMenores = rotasEscolhidas.last().getDistanceAllLinks();


                }
                continue;
            }

            if (expand.getDistanceAllLinks() > maiorEntreAsMenores) {//não adianta continuar procurando nesta rota

            } else { //procurar mais rotas a partir desta

                for (Node no : mesh.getAdjacents(expand.getDestino())) {

                    Route rAux = expand.clone();
                    rAux.addNode(no);

                    rotasEmContrucao.add(rAux);
                }

            }


        }


        return new ArrayList<Route>(rotasEscolhidas);
    }

    private boolean possuiLoop(Route r) {
        HashSet<String> nos = new HashSet<>();
        for (Node n : r.getNodeList()) {
            if (nos.contains(n.getName())) return true;
            nos.add(n.getName());
        }
        return false;
    }

    public List<Route> getRoutes(Node n1, Node n2) {
        return this.routesForAllPairs.get(n1.getName() + DIV + n2.getName());
    }

}
